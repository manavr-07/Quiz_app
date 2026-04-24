package api;

import model.ApiResponse;
import model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight JSON parser for the quiz API response.
 *
 * Handles the fixed structure:
 * {
 *   "regNo":    "...",
 *   "setId":    "...",
 *   "pollIndex": N,
 *   "events": [
 *     { "roundId": "...", "participant": "...", "score": N },
 *     ...
 *   ]
 * }
 *
 * No external dependencies — uses only java.util.regex.
 */
public class JsonParser {

    // Patterns for top-level scalar fields
    private static final Pattern P_STRING  = Pattern.compile("\"(%s)\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern P_INT     = Pattern.compile("\"(%s)\"\\s*:\\s*(-?\\d+)");

    // Pattern to extract each object inside the events array
    private static final Pattern P_EVENTS  = Pattern.compile("\\{[^{}]*\\}");

    // ── public API ────────────────────────────────────────────────────────────

    public static ApiResponse parseApiResponse(String json) {
        if (json == null || json.isBlank()) return null;

        ApiResponse resp = new ApiResponse();
        resp.setRegNo(extractString(json, "regNo"));
        resp.setSetId(extractString(json, "setId"));
        resp.setPollIndex(extractInt(json, "pollIndex", 0));
        resp.setEvents(extractEvents(json));
        return resp;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static String extractString(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static int extractInt(String json, String key, int defaultVal) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : defaultVal;
    }

    /**
     * Extract the raw JSON array string for "events", then parse each object.
     * Strategy: find the array between the first '[' after '"events"' and its matching ']'.
     */
    private static List<Event> extractEvents(String json) {
        List<Event> events = new ArrayList<>();

        int eventsKeyIdx = json.indexOf("\"events\"");
        if (eventsKeyIdx == -1) return events;

        int arrayStart = json.indexOf('[', eventsKeyIdx);
        if (arrayStart == -1) return events;

        int arrayEnd = findMatchingBracket(json, arrayStart);
        if (arrayEnd == -1) return events;

        String arrayContent = json.substring(arrayStart, arrayEnd + 1);

        // Each event is a {...} object — extract them one by one
        Matcher m = P_EVENTS.matcher(arrayContent);
        while (m.find()) {
            String obj = m.group();
            Event ev = new Event();
            ev.setRoundId(extractString(obj, "roundId"));
            ev.setParticipant(extractString(obj, "participant"));
            ev.setScore(extractInt(obj, "score", 0));
            events.add(ev);
        }
        return events;
    }

    /** Finds the index of the ']' that closes the '[' at startIdx. */
    private static int findMatchingBracket(String json, int startIdx) {
        int depth = 0;
        for (int i = startIdx; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    // ── serialisation ─────────────────────────────────────────────────────────

    /** Build the JSON string for the POST body without any library. */
    public static String buildSubmitBody(String regNo,
                                         java.util.List<model.LeaderboardEntry> leaderboard) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"regNo\":\"").append(escape(regNo)).append("\",\"leaderboard\":[");

        for (int i = 0; i < leaderboard.size(); i++) {
            model.LeaderboardEntry e = leaderboard.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"participant\":\"").append(escape(e.getParticipant()))
              .append("\",\"totalScore\":").append(e.getTotalScore()).append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
