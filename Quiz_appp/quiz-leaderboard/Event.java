package model;

public class Event {
    private String roundId;
    private String participant;
    private int score;

    public Event() {}

    public String getRoundId()       { return roundId; }
    public String getParticipant()   { return participant; }
    public int    getScore()         { return score; }

    public void setRoundId(String roundId)         { this.roundId = roundId; }
    public void setParticipant(String participant)  { this.participant = participant; }
    public void setScore(int score)                { this.score = score; }

    @Override
    public String toString() {
        return "Event{roundId='" + roundId + "', participant='" + participant + "', score=" + score + "}";
    }
}
