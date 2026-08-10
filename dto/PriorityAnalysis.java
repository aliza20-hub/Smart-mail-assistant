package com.smartmail.backend.dto;

public class PriorityAnalysis {
    private int urgencyScore;      // 0-100
    private String sentiment;      // Positive / Neutral / Negative / Urgent-Negative etc.
    private String category;       // e.g. "Client escalation", "Newsletter", "Meeting request"
    private String reasoning;      // short human-readable explanation

    public PriorityAnalysis() {}

    public PriorityAnalysis(int urgencyScore, String sentiment, String category, String reasoning) {
        this.urgencyScore = urgencyScore;
        this.sentiment = sentiment;
        this.category = category;
        this.reasoning = reasoning;
    }

    public int getUrgencyScore() { return urgencyScore; }
    public void setUrgencyScore(int urgencyScore) { this.urgencyScore = urgencyScore; }

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
}
