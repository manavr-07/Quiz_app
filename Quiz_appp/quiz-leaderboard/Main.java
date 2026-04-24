import api.QuizApiClient;
import model.ApiResponse;
import model.LeaderboardEntry;
import service.LeaderboardService;

import java.util.List;
import java.util.Map;

/**
 * Entry point.
 *
 * Flow:
 *   1. Poll GET endpoint 10 times (poll=0..9), 5-second gap between calls.
 *   2. Deduplicate events by (roundId + participant) using a HashSet.
 *   3. Aggregate scores into a HashMap<participant, totalScore>.
 *   4. Sort leaderboard descending by totalScore.
 *   5. Submit once via POST.
 */
public class Main {

    private static final String REG_NO         = "REG12345";   // ← replace with your regNo
    private static final int    TOTAL_POLLS     = 10;
    private static final long   POLL_DELAY_MS   = 5_000;       // 5 seconds between polls

    public static void main(String[] args) throws InterruptedException {

        // Allow regNo to be passed as a CLI argument: java Main REG12345
        String regNo = (args.length > 0) ? args[0] : REG_NO;
        System.out.println("=== Quiz Leaderboard Aggregator ===");
        System.out.println("regNo : " + regNo);
        System.out.println("polls : " + TOTAL_POLLS + "  (poll=0.." + (TOTAL_POLLS - 1) + ")");
        System.out.println("delay : " + (POLL_DELAY_MS / 1000) + "s between calls");
        System.out.println("===================================\n");

        QuizApiClient      client  = new QuizApiClient(regNo);
        LeaderboardService service = new LeaderboardService();

        // ── Polling loop ──────────────────────────────────────────────────────
        for (int poll = 0; poll < TOTAL_POLLS; poll++) {

            System.out.printf("[POLL %2d/%d] Fetching …%n", poll, TOTAL_POLLS - 1);

            ApiResponse response = client.fetchPoll(poll);

            if (response == null) {
                System.out.printf("[POLL %2d/%d] No data — continuing to next poll.%n%n", poll, TOTAL_POLLS - 1);
            } else {
                int totalEvents = response.getEvents() != null ? response.getEvents().size() : 0;
                int newEvents   = service.processResponse(response);

                System.out.printf("[POLL %2d/%d] Events received: %d  |  New (unique): %d%n",
                        poll, TOTAL_POLLS - 1, totalEvents, newEvents);

                // Print running scores after each poll
                System.out.println("  [SCORES] " + service.getScoreMap());
                System.out.println();
            }

            // 5-second delay — skip after the very last poll
            if (poll < TOTAL_POLLS - 1) {
                System.out.println("  Waiting " + (POLL_DELAY_MS / 1000) + "s …\n");
                Thread.sleep(POLL_DELAY_MS);
            }
        }

        // ── Build leaderboard ─────────────────────────────────────────────────
        List<LeaderboardEntry> leaderboard = service.buildLeaderboard();
        int total = service.computeTotal();

        System.out.println("\n=== FINAL LEADERBOARD ===");
        for (int i = 0; i < leaderboard.size(); i++) {
            LeaderboardEntry e = leaderboard.get(i);
            System.out.printf("  #%-2d  %-20s  %d%n", i + 1, e.getParticipant(), e.getTotalScore());
        }
        System.out.println("  ─────────────────────────────");
        System.out.printf("  TOTAL                       %d%n%n", total);

        // ── Submit once ───────────────────────────────────────────────────────
        client.submitLeaderboard(leaderboard, total);

        System.out.println("\n=== Done ===");
    }
}
