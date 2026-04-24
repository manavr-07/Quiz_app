package model;

public class LeaderboardEntry {
    private String participant;
    private int totalScore;

    public LeaderboardEntry(String participant, int totalScore) {
        this.participant = participant;
        this.totalScore  = totalScore;
    }

    public String getParticipant() { return participant; }
    public int    getTotalScore()  { return totalScore; }

    public void setParticipant(String participant) { this.participant = participant; }
    public void setTotalScore(int totalScore)       { this.totalScore = totalScore; }

    @Override
    public String toString() {
        return participant + " → " + totalScore;
    }
}
