package com.smartmail.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmail.backend.dto.PriorityAnalysis;
import com.smartmail.backend.dto.ReplyRequest;
import com.smartmail.backend.service.GeminiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/analyze")
public class PriorityController {

    private final GeminiService geminiService;
    private final ObjectMapper mapper = new ObjectMapper();

    public PriorityController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/priority")
    public Mono<PriorityAnalysis> analyzePriority(@Valid @RequestBody ReplyRequest request) {
        String system = """
                You are an email triage assistant. Analyze the email and respond with
                STRICT JSON ONLY (no markdown fences, no commentary) matching exactly
                this shape:
                {"urgencyScore": <integer 0-100>, "sentiment": "<Positive|Neutral|Negative|Urgent>",
                 "category": "<short 2-4 word label>", "reasoning": "<one short sentence>"}
                """;

        String userPrompt = "Email to analyze:\n---\n" + request.getOriginalEmail() + "\n---";

        return geminiService.generate(system, userPrompt)
                .map(this::parsePriorityJson);
    }

    private PriorityAnalysis parsePriorityJson(String raw) {
        try {
            // Gemini occasionally wraps JSON in ```json fences despite instructions; strip them.
            String cleaned = raw.trim()
                    .replaceAll("^```json", "")
                    .replaceAll("^```", "")
                    .replaceAll("```$", "")
                    .trim();

            JsonNode node = mapper.readTree(cleaned);
            return new PriorityAnalysis(
                    node.path("urgencyScore").asInt(50),
                    node.path("sentiment").asText("Neutral"),
                    node.path("category").asText("Uncategorized"),
                    node.path("reasoning").asText("")
            );
        } catch (Exception e) {
            return new PriorityAnalysis(50, "Neutral", "Uncategorized",
                    "Could not parse model output.");
        }
    }
}
