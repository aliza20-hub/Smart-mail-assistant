package com.smartmail.backend.controller;

import com.smartmail.backend.dto.ReplyRequest;
import com.smartmail.backend.service.GeminiService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/reply")
public class StreamReplyController {

    private final GeminiService geminiService;

    public StreamReplyController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    /**
     * Streams the AI reply back to the browser token-by-token as text/event-stream.
     * This is what powers the "typing live" effect in the UI.
     */
    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamReply(@Valid @RequestBody ReplyRequest request) {
        String system =
                """
                You are an email assistant drafting a reply on behalf of the user.
                Write ONLY the reply body text, no subject line, no "Dear/Hi" placeholder
                brackets, no explanation of what you're doing. Keep it natural and concise.
                """ ;

        String userPrompt =
                """
                Original email:
                ---
                %s
                ---
                Extra instructions from the user (may be empty): %s

                Write a suitable reply.
                """
                        .formatted(request.getOriginalEmail(),
                (request.getInstructions() == null || request.getInstructions().isBlank())
                        ? "none" : request.getInstructions());

        return geminiService.streamGenerate(system, userPrompt);
    }
}
