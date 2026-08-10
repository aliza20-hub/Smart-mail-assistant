package com.smartmail.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Thin reactive wrapper around Google's Gemini generateContent / streamGenerateContent
 * endpoints. Two modes are exposed:
 *  - generate(prompt)        -> one-shot Mono<String>, used for tone replies & analysis
 *  - streamGenerate(prompt)  -> Flux<String> of incremental text chunks, used to power
 *                               the live "typing" reply in the UI (the unique bit).
 */
@Service
public class GeminiService {

    private final WebClient geminiWebClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.request-timeout-seconds}")
    private long timeoutSeconds;

    public GeminiService(WebClient geminiWebClient) {
        this.geminiWebClient = geminiWebClient;
    }

    /** One-shot, non-streaming completion. Returns the full text as a single Mono. */
    public Mono<String> generate(String systemInstruction, String userPrompt) {
        Map<String, Object> body = buildRequestBody(systemInstruction, userPrompt);

        return geminiWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/{model}:generateContent")
                        .queryParam("key", apiKey)
                        .build(model))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .map(this::extractTextFromFullResponse);
    }

    /** Streaming completion: emits text deltas as they arrive from Gemini's SSE endpoint. */
    public Flux<String> streamGenerate(String systemInstruction, String userPrompt) {
        Map<String, Object> body = buildRequestBody(systemInstruction, userPrompt);

        return geminiWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/{model}:streamGenerateContent")
                        .queryParam("alt", "sse")
                        .queryParam("key", apiKey)
                        .build(model))
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .mapNotNull(this::extractTextFromStreamChunk)
                .filter(text -> !text.isEmpty());
    }

    private Map<String, Object> buildRequestBody(String systemInstruction, String userPrompt) {
        return Map.of(
                "system_instruction", Map.of(
                        "parts", new Object[]{ Map.of("text", systemInstruction) }
                ),
                "contents", new Object[]{
                        Map.of("role", "user", "parts", new Object[]{ Map.of("text", userPrompt) })
                },
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 1024
                )
        );
    }

    private String extractTextFromFullResponse(JsonNode root) {
        JsonNode textNode = root.path("candidates").path(0)
                .path("content").path("parts").path(0).path("text");
        return textNode.isMissingNode() ? "" : textNode.asText("");
    }

    /**
     * Each SSE "data:" line from Gemini is itself a JSON chunk shaped like the full
     * response. WebClient's bodyToFlux(String.class) with alt=sse already strips the
     * "data: " prefix and delivers one JSON object per element.
     */
    private String extractTextFromStreamChunk(String jsonChunk) {
        try {
            JsonNode root = mapper.readTree(jsonChunk);
            JsonNode textNode = root.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text");
            return textNode.isMissingNode() ? "" : textNode.asText("");
        } catch (Exception e) {
            return "";
        }
    }
}
