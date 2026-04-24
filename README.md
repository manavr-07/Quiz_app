# Quiz Leaderboard Aggregator

> **Java | Lightweight Backend Application**

A standalone Java 17 application that interacts with a quiz API, removes duplicate events, calculates participant scores, and submits a final ranked leaderboard — ensuring the submission happens only once.

**Repository:** [manavr-07/Quiz_app](https://github.com/manavr-07/Quiz_app)

---



---

## Overview

The application follows a structured pipeline to process quiz data efficiently:

```
Polling → Deduplication → Aggregation → Sorting → Submission
```

This ensures data accuracy and prevents duplicate score inflation while maintaining a single submission guarantee.

---

## Processing Pipeline

### 1. Polling

The system makes 10 API calls to:
```
GET /quiz/messages?regNo=...&poll=0..9
```

**Details:**
- Each request is spaced by a **5-second delay**
- If a request fails, it **retries up to 3 times** using exponential backoff:
  - 2 seconds → 4 seconds → 8 seconds
- If all retries fail, the poll is **skipped** (execution continues)

---

### 2. Deduplication

Each event is identified using a unique key:
```
roundId + "_" + participant
```

**Implementation:**
- Uses `HashSet<String>` to track processed events
- Duplicate entries are ignored to prevent score inflation
- Ensures each event is counted exactly once

---

### 3. Aggregation

Scores are accumulated efficiently using:
```java
HashMap<String, Integer>
```

**Process:**
- Each participant's total score is updated only for unique events
- Maintains running totals across all polls

---

### 4. Sorting

Final leaderboard is sorted with the following rules:

| Priority | Sort Criteria | Order |
|----------|---------------|-------|
| 1 | Total Score | Descending (highest first) |
| 2 | Participant Name | Alphabetical (A-Z) |

This ensures consistent and stable ranking.

---

### 5. Submission

Final result is submitted once via:
```
POST /quiz/submit
```

**Details:**
- Payload contains `regNo` and computed leaderboard
- **No duplicate submissions** are made
- Submission happens only after all processing is complete

---

## Data Structures

| Structure | Purpose | Time Complexity |
|-----------|---------|-----------------|
| `HashSet<String>` | Detect duplicate events efficiently | O(1) lookup |
| `HashMap<String, Integer>` | Store cumulative scores | O(1) access |
| `List<LeaderboardEntry>` | Maintain sorted leaderboard | O(n log n) sort |

---

## 📁 Project Structure

```
quiz-leaderboard/
├── Main.java
├── QuizApiClient.java
├── JsonParser.java
├── LeaderboardService.java
├── ApiResponse.java
├── Event.java
├── LeaderboardEntry.java
├── quiz-leaderboard.jar
├── README.md
└── .gitignore
```

---

## Key Features

**No external dependencies** — pure Java 17  
**Built-in retry mechanism** with exponential backoff  
**Efficient duplicate handling** across polls  
**Accurate score computation** with deduplication  
**Deterministic leaderboard sorting** for consistency  
**Single submission guarantee** — no duplicates  

---

## Requirements

- **Java 17** or higher
- **No additional libraries** required

---

## Setup & Execution

### Clone Repository

```bash
git clone https://github.com/manavr-07/Quiz_app
cd Quiz_app/Quiz_appp/quiz-leaderboard
```

---

### Option 1: Run Using Pre-built JAR

```bash
java -jar quiz-leaderboard.jar YOUR_REG_NO
```

---

### Option 2: Compile & Run from Source

```bash
javac *.java
java Main YOUR_REG_NO
```

---

### Option 3: Run with Maven

```bash
mvn package -DskipTests
java -jar target/quiz-leaderboard-1.0.jar YOUR_REG_NO
```

---

## Example Output

```
=== Quiz Leaderboard Aggregator ===
regNo : REG12345
polls : 10 (0..9)
delay : 5 seconds
-----------------------------------
[POLL 0] Fetching...
[DUPLICATE] Skipped: round1_Alice
New events: 3
Scores: {Alice=120, Bob=95}
...
=== FINAL LEADERBOARD ===
1. Bob    145
2. Alice  120
3. Carol   80
[SUBMIT] Status: 200
[RESULT] Correct: true
```

---

## Validation Checklist

- [ ] 10 API calls executed (poll 0–9)
- [ ] 5-second delay maintained between calls
- [ ] Duplicate events filtered correctly
- [ ] Scores aggregated accurately
- [ ] Leaderboard sorted properly (score desc, name asc)
- [ ] Single POST submission executed
- [ ] Retry logic handled failures gracefully
- [ ] Application runs via single command

---

## Contribution

We welcome contributions! Follow these steps:

1. **Fork** the repository
2. **Create** a new branch (`git checkout -b feature/your-feature`)
3. **Commit** your changes (`git commit -m 'Add your message'`)
4. **Push** to the branch (`git push origin feature/your-feature`)
5. **Submit** a Pull Request

---

**Maintainer:** [manavr-07](https://github.com/manavr-07)
