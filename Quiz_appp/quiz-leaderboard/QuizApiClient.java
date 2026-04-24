package api;

import model.ApiResponse;
import model.LeaderboardEntry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class QuizApiClient {

    private static final String BASE_URL   = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final int    MAX_RETRY  = 3;
    private static final long   BACKOFF_MS = 2_000;   // doubles on each retry

    private final HttpClient http;
    private final String     regNo;

    public QuizApiClient(String regNo) {
        this.regNo = regNo;
        this.http  = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // ── GET /quiz/messages?regNo=...&poll=... ─────────────────────────────────

    public ApiResponse fetchPoll(int pollIndex) {
        String url = BASE_URL + "/quiz/messages?regNo=" + regNo + "&poll=" + pollIndex;

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(15))
                        .GET()
                        .build();

                HttpResponse<String> response =
                        http.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String body = response.body();
                    System.out.println("  [RAW ] " + body);
                    return JsonParser.parseApiResponse(body);
                }

                System.out.printf("  [WARN] Poll %d attempt %d: HTTP %d%n",
                        pollIndex, attempt, response.statusCode());

            } catch (Exception e) {
                System.out.printf("  [WARN] Poll %d attempt %d failed: %s%n",
                        pollIndex, attempt, e.getMessage());
            }

            if (attempt < MAX_RETRY) {
                long delay = BACKOFF_MS * (1L << (attempt - 1));
                System.out.printf("  [INFO] Retrying in %d ms …%n", delay);
                sleep(delay);
            }
        }

        System.out.printf("  [ERROR] Poll %d failed after %d attempts — skipping.%n",
                pollIndex, MAX_RETRY);
        return null;
    }

    // ── POST /quiz/submit ─────────────────────────────────────────────────────

    public void submitLeaderboard(List<LeaderboardEntry> leaderboard, int total) {
        try {
            String json = JsonParser.buildSubmitBody(regNo, leaderboard);

            System.out.println("\n[SUBMIT] Posting leaderboard …");
            System.out.println("[SUBMIT] Payload  : " + json);
            System.out.println("[SUBMIT] Computed total: " + total);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/quiz/submit"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    http.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("[SUBMIT] HTTP status : " + response.statusCode());
            System.out.println("[SUBMIT] Response    : " + response.body());

            parseAndLogSubmitResponse(response.body());

        } catch (Exception e) {
            System.out.println("[ERROR] Submission failed: " + e.getMessage());
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void parseAndLogSubmitResponse(String body) {
        if (body == null || body.isBlank()) return;
        try {
            String isCorrect      = extractField(body, "isCorrect");
            String submittedTotal = extractField(body, "submittedTotal");
            String expectedTotal  = extractField(body, "expectedTotal");
            if (isCorrect      != null) System.out.println("[SUBMIT] isCorrect      : " + isCorrect);
            if (submittedTotal != null) System.out.println("[SUBMIT] submittedTotal : " + submittedTotal);
            if (expectedTotal  != null) System.out.println("[SUBMIT] expectedTotal  : " + expectedTotal);
        } catch (Exception ignored) {}
    }

    private String extractField(String json, String key) {
        java.util.regex.Matcher m =
            java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*(\"[^\"]*\"|[^,}\\s]+)")
                .matcher(json);
        return m.find() ? m.group(1).replace("\"", "") : null;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}
