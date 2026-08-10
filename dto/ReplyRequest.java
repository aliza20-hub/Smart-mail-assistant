package com.smartmail.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class ReplyRequest {

    @NotBlank(message = "originalEmail must not be blank")
    private String originalEmail;

    /** Optional free-text hint from the user , e.g. "decline politely, suggest next week" */
    private String instructions;

    public ReplyRequest() {}

    public ReplyRequest(String originalEmail, String instructions) {
        this.originalEmail = originalEmail;
        this.instructions = instructions;
    }

    public String getOriginalEmail() { return originalEmail; }
    public void setOriginalEmail(String originalEmail) { this.originalEmail = originalEmail; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
