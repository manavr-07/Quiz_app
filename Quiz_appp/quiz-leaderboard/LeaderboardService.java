package service;

import model.ApiResponse;
import model.Event;
import model.LeaderboardEntry;

import java.util.*;

/**
 * Core processing logic — completely decoupled from HTTP concerns.
 *
 * State maintained across all polls:
 *   seenKeys  – guards against counting the same (roundId + participant) twice
 *   scoreMap  – running total per participant
 */
public class LeaderboardService {

    private final Set<String>     seenKeys = new HashSet<>();
    private final Map<String, Integer> scoreMap = new HashMap<>();

    /**
     * Process a single poll response.
     * Returns the number of NEW (non-duplicate) events added.
     */
    public int processResponse(ApiResponse response) {
        if (response == null || response.getEvents() == null) {
            return 0;
        }

        int newEvents = 0;

        for (Event event : response.getEvents()) {
            if (!isValid(event)) {
                System.out.println("  [SKIP] Invalid event (null fields): " + event);
                continue;
            }

            String key = event.getRoundId() + "_" + event.getParticipant();

            if (seenKeys.contains(key)) {
                System.out.println("  [DUP ] Skipping duplicate: " + key);
                continue;
            }

            seenKeys.add(key);
            scoreMap.merge(event.getParticipant(), event.getScore(), Integer::sum);
            newEvents++;
        }

        return newEvents;
    }

    /**
     * Build the final leaderboard sorted by totalScore descending.
     * Ties are broken alphabetically by participant name (stable, predictable).
     */
    public List<LeaderboardEntry> buildLeaderboard() {
        return scoreMap.entrySet().stream()
                .map(e -> new LeaderboardEntry(e.getKey(), e.getValue()))
                .sorted(Comparator
                        .comparingInt(LeaderboardEntry::getTotalScore).reversed()
                        .thenComparing(LeaderboardEntry::getParticipant))
                .toList();
    }

    /** Sum of all unique scores across all participants. */
    public int computeTotal() {
        return scoreMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Snapshot of current scores for logging (unmodifiable view). */
    public Map<String, Integer> getScoreMap() {
        return Collections.unmodifiableMap(scoreMap);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private boolean isValid(Event event) {
        return event.getRoundId()     != null && !event.getRoundId().isBlank()
            && event.getParticipant() != null && !event.getParticipant().isBlank();
    }
}
