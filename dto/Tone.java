package com.smartmail.backend.dto;

public enum Tone {
    PROFESSIONAL("Professional", "Formal, concise, businesslike. No slang, no emoji."),
    FRIENDLY("Friendly", "Warm and personable, still efficient. Light, natural tone."),
    FIRM("Firm", "Direct and boundary-setting, but respectful. No hedging.");

    private final String label;
    private final String styleGuide;

    Tone(String label, String styleGuide) {
        this.label = label;
        this.styleGuide = styleGuide;
    }

    public String getLabel() { return label; }
    public String getStyleGuide() { return styleGuide; }
}
