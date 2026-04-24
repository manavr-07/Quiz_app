# Quiz Leaderboard Aggregator

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A zero-dependency Java 17 application that polls a quiz API, deduplicates events, aggregates scores, and submits a sorted leaderboard — exactly once.

**Repository:** [https://github.com/ADITYA-7565/quiz-leaderboard](https://github.com/manavr-07/Quiz_app)

---

## Approach

```
Poll (×10, 5s gap) → Deduplicate → Aggregate scores → Sort → Submit once
```

1. **Poll** — Call `GET /quiz/messages?regNo=...&poll=0..9` ten times, with a 5-second pause between each call. Transient failures are retried up to 3 times with exponential backoff (2 s → 4 s → 8 s). A poll that fails completely is logged and skipped rather than aborting the whole run.

2. **Deduplicate** — Each event carries a `roundId` and a `participant`. The dedup key is `roundId + "_" + participant`. Before crediting any score, we check a `HashSet<String>`. If the key is already present the event is silently skipped; otherwise it is recorded.

3. **Aggregate** — Scores for accepted events are accumulated in a `HashMap<String, Integer>` keyed by participant name.

4. **Sort** — After all 10 polls, the map is converted to a `List<LeaderboardEntry>` and sorted descending by `totalScore`. Ties are broken alphabetically so the order is always stable.

5. **Submit** — `POST /quiz/submit` is called exactly once with the final leaderboard JSON.

---

## Why deduplication is needed

The same quiz event can appear in multiple polls (the API deliberately replays past events). Counting a duplicate inflates a participant's score and produces a wrong leaderboard total. The `HashSet` is the single source of truth: an event is counted at most once no matter how many polls it appears in.

---

## Data structures

| Structure | Purpose |
|-----------|---------|
| `HashSet<String> seenKeys` | O(1) lookup to detect duplicates across all polls |
| `HashMap<String, Integer> scoreMap` | Running total per participant |
| `List<LeaderboardEntry>` | Sorted final leaderboard (immutable after sort) |

---

## Project layout

```
quiz-leaderboard/
├── Main.java                   ← entry point, polling loop
├── QuizApiClient.java          ← HTTP GET/POST with retry/backoff
├── JsonParser.java             ← lightweight JSON parser (no external deps)
├── LeaderboardService.java     ← dedup + aggregation logic
├── ApiResponse.java            ← API response model
├── Event.java                  ← quiz event model
├── LeaderboardEntry.java       ← leaderboard entry model
├── quiz-leaderboard.jar        ← pre-built runnable JAR
├── README.md                   ← this file
└── .gitignore                  ← git ignore rules
```
## Features

- **Zero Dependencies**: Pure Java 17 with no external libraries
- **Robust Error Handling**: Retry logic with exponential backoff for transient failures
- **Event Deduplication**: Prevents double-counting of quiz events across polls
- **Accurate Scoring**: Maintains correct leaderboard totals
- **Stable Sorting**: Consistent ordering with alphabetical tie-breaking
- **Single Submission**: Ensures leaderboard is posted exactly once

## Requirements

- Java 17 or later
- No other dependencies required

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/ADITYA-7565/quiz-leaderboard.git
   cd quiz-leaderboard
   ```

2. Run with your registration number:
   ```bash
   java -jar quiz-leaderboard.jar YOUR_REG_NO
   ```

## Usage

### Option A — Pre-built JAR (fastest)

```bash
java -jar quiz-leaderboard.jar REG12345
```

Replace `REG12345` with your actual `regNo`. If you omit the argument, the default inside `Main.java` is used.

### Option B — Compile and run from source

```bash
javac *.java
java Main REG12345
```

### Option C — Maven (if pom.xml exists)

```bash
mvn package -DskipTests
java -jar target/quiz-leaderboard-1.0.jar REG12345
```

## Sample Output

```
=== Quiz Leaderboard Aggregator ===
regNo : REG12345
polls : 10  (poll=0..9)
delay : 5s between calls
===================================

[POLL  0/ 9] Fetching …
  [RAW ] {"regNo":"REG12345","setId":"SET_A","pollIndex":0,"events":[...]}
  [DUP ] Skipping duplicate: round1_Alice
[POLL  0/ 9] Events received: 4  |  New (unique): 3
  [SCORES] {Alice=120, Bob=95, Carol=80}

  Waiting 5s …

[POLL  1/ 9] Fetching …
  [RAW ] {"regNo":"REG12345","setId":"SET_A","pollIndex":1,"events":[...]}
[POLL  1/ 9] Events received: 3  |  New (unique): 2
  [SCORES] {Alice=120, Bob=145, Carol=80}
...

=== FINAL LEADERBOARD ===
  #1   Bob                  145
  #2   Alice                120
  #3   Carol                80
  ─────────────────────────────
  TOTAL                       345

[SUBMIT] Posting leaderboard …
[SUBMIT] Payload  : {"regNo":"REG12345","leaderboard":[...]}
[SUBMIT] Computed total: 345
[SUBMIT] HTTP status : 200
[SUBMIT] Response    : {"isCorrect":true,"submittedTotal":345,"expectedTotal":345}
[SUBMIT] isCorrect      : true
[SUBMIT] submittedTotal : 345
[SUBMIT] expectedTotal  : 345

=== Done ===
```

## Sanity Checklist

- [x] Exactly 10 polls (`poll=0..9`)
- [x] 5-second delay between polls
- [x] Dedup key = `roundId + "_" + participant`
- [x] Only unique events contribute to scores
- [x] Leaderboard sorted descending by `totalScore`
- [x] POST called exactly once at the end
- [x] Transient failures retried with backoff; a fully-failed poll is skipped, not fatal
- [x] Runs end-to-end with a single `java -jar` command

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
