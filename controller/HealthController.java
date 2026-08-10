package com.smartmail.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${gemini.api-key}")
    private String apiKey;
@GetMapping("/working")
public String Working(){return "Succesful"; }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "geminiKeyConfigured", apiKey != null && !apiKey.isBlank()
        );
    }
}
