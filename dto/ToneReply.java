package com.smartmail.backend.dto;

public class ToneReply {
    private String tone;
    private String label;
    private String reply;

    public ToneReply() {}

    public ToneReply(String tone, String label, String reply) {
        this.tone = tone;
        this.label = label;
        this.reply = reply;
    }

    public String getTone() { return tone; }
    public void setTone(String tone) {
        this.tone = tone; }

    public String getLabel() { return label; }
    public void setLabel(String label) {
        this.label = label; }

    public String getReply() { return reply; }
    public void setReply(String reply) {
        this.reply = reply; }
}
