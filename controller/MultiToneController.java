package com.smartmail.backend.controller;

import com.smartmail.backend.dto.ReplyRequest;
import com.smartmail.backend.dto.Tone;
import com.smartmail.backend.dto.ToneReply;
import com.smartmail.backend.service.GeminiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/reply")
public class MultiToneController {

    private final GeminiService geminiService;

    public MultiToneController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    /**
     * Fires 3 Gemini calls in parallel (one per Tone) and returns all three once
     * they've all resolved. Uses Flux.merge so the 3 calls are genuinely concurrent,
     * not sequential.
     */
    @PostMapping("/multi-tone")
    public Mono<List<ToneReply>> multiTone(@Valid @RequestBody ReplyRequest request) {
        List<Mono<ToneReply>> calls = List.of(Tone.values()).stream()
                .map(tone -> generateForTone(tone, request))
                .toList();

        return Flux.merge(calls).collectList();
    }

    private Mono<ToneReply> generateForTone(Tone tone, ReplyRequest request) {
        String system = """
                You are an email assistant. Write ONLY the reply body text in a %s tone.
                Style guide: %s
                No subject line, no explanations, no markdown formatting.
                """.formatted(tone.getLabel(), tone.getStyleGuide());

        String userPrompt = """
                Original email:
                ---
                %s
                ---
                Extra instructions from the user (may be empty): %s
                """.formatted(request.getOriginalEmail(),
                (request.getInstructions() == null || request.getInstructions().isBlank())
                        ? "none" : request.getInstructions());

        return geminiService.generate(system, userPrompt)
                .map(text -> new ToneReply(tone.name(), tone.getLabel(), text.trim()));
    }
}
