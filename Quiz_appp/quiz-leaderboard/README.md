Here’s a clean paraphrased version of your README with improved flow and the updated repo link:

⸻

Quiz Leaderboard Aggregator

Java | Lightweight Backend Application

A standalone Java 17 application that interacts with a quiz API, removes duplicate events, calculates participant scores, and submits a final ranked leaderboard — ensuring the submission happens only once.

Repository: https://github.com/manavr-07/Quiz_app

⸻

Overview

The application follows a structured pipeline:

Polling → Deduplication → Aggregation → Sorting → Submission

1. Polling

The system makes 10 API calls to:

GET /quiz/messages?regNo=...&poll=0..9

* Each request is spaced by a 5-second delay
* If a request fails, it retries up to 3 times using exponential backoff:
    * 2 seconds → 4 seconds → 8 seconds
* If all retries fail, the poll is skipped (does not stop execution)

⸻

2. Deduplication

Each event is identified using:

roundId + "_" + participant

* A HashSet<String> is used to track processed events
* Duplicate entries are ignored to prevent score inflation

⸻

3. Aggregation

* Scores are accumulated using:

HashMap<String, Integer>

* Each participant’s total score is updated only for unique events

⸻

4. Sorting

* Final leaderboard is sorted in descending order of totalScore
* In case of equal scores, participants are ordered alphabetically
* Ensures consistent and stable ranking

⸻

5. Submission

* Final result is submitted once via:

POST /quiz/submit

* Payload contains regNo and computed leaderboard
* No duplicate submissions are made

⸻

Why Deduplication Matters

The API may return repeated events across multiple polls.
Without filtering duplicates, scores would be incorrectly inflated.

The HashSet ensures:

* Each event is counted exactly once
* Leaderboard remains accurate and fair

⸻

Data Structures Used

Structure	Purpose
HashSet<String>	Detect duplicate events efficiently
HashMap<String, Integer>	Store cumulative scores
List<LeaderboardEntry>	Maintain sorted leaderboard

⸻

Project Structure

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

⸻

Key Features

* No external dependencies (pure Java 17)
* Built-in retry mechanism with exponential backoff
* Efficient duplicate handling across polls
* Accurate score computation
* Deterministic leaderboard sorting
* Single submission guarantee

⸻

Requirements

* Java 17 or higher
* No additional libraries required

⸻

Setup & Execution

Clone Repository

git clone https://github.com/manavr-07/Quiz_app
cd Quiz_app

⸻

Run Using Pre-built JAR

java -jar quiz-leaderboard.jar YOUR_REG_NO

⸻

Compile & Run from Source

javac *.java
java Main YOUR_REG_NO

⸻

Run with Maven (if applicable)

mvn package -DskipTests
java -jar target/quiz-leaderboard-1.0.jar YOUR_REG_NO

⸻

Example Output

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

⸻

Validation Checklist

* 10 API calls executed (poll 0–9)
* 5-second delay maintained
* Duplicate events filtered correctly
* Scores aggregated accurately
* Leaderboard sorted properly
* Single POST submission
* Retry logic handled failures gracefully
* Runs via a single command

⸻

Contribution

* Fork the repository
* Create a new branch
* Commit your changes
* Push and raise a Pull Request

⸻

If you want, I can also:

* Make this ATS-friendly for resume projects
* Add badges + GitHub polish (stars, shields, etc.)
* Or convert it into a perfect project description for interviews
