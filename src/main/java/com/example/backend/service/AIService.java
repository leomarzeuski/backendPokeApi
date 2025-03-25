package com.example.backend.service;

import com.example.backend.models.ai.AIRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {

    private final WebClient aiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.model:gpt-3.5-turbo}")
    private String aiModel;

    @Autowired
    public AIService(@Qualifier("aiWebClient") WebClient aiWebClient, ObjectMapper objectMapper) {
        this.aiWebClient = aiWebClient;
        this.objectMapper = objectMapper;
    }

    public Mono<String> generateContent(AIRequest request) {
        // Create the AI request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiModel);

        // Create the messages array with system and user messages
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", getSystemPrompt(request.getContentType()));

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", request.getPrompt());

        requestBody.put("messages", new Object[] { systemMessage, userMessage });

        // Set temperature based on content type
        double temperature = getTemperatureForContentType(request.getContentType());
        requestBody.put("temperature", temperature);

        // Set max tokens if specified
        if (request.getMaxLength() != null) {
            int maxTokens = request.getMaxLength();
            requestBody.put("max_tokens", maxTokens);
        }

        // Make the API call
        return aiWebClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        JsonNode rootNode = objectMapper.readTree(responseBody);
                        String content = rootNode.path("choices").path(0).path("message").path("content").asText();
                        return content;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse AI response", e);
                    }
                });
    }

    private String getSystemPrompt(String contentType) {
        switch (contentType != null ? contentType.toLowerCase() : "") {
            case "story":
                return "You are a creative writer specializing in Pokémon stories. " +
                        "Create engaging, imaginative stories that capture the essence of the Pokémon world. " +
                        "Keep the content appropriate for all ages.";

            case "strategy":
                return "You are a competitive Pokémon battle expert. " +
                        "Create detailed, accurate battle strategies focusing on team composition, " +
                        "move selection, item choices, and battle tactics. " +
                        "Your advice should be grounded in the mechanics of the Pokémon games.";

            case "pokedex":
                return "You are Professor Oak, the world's leading Pokémon researcher. " +
                        "Create detailed, scientific Pokédex entries that describe Pokémon's physical characteristics, "
                        +
                        "habitat, behavior, and special abilities. Your entries should be informative and fascinating.";

            case "explanation":
                return "You are a Pokémon expert who can clearly explain relationships between different Pokémon. " +
                        "Your explanations should be concise, accurate, and highlight meaningful connections " +
                        "between Pokémon based on their types, stats, evolution lines, and other characteristics.";

            default:
                return "You are a helpful Pokémon expert. Provide detailed, accurate information about Pokémon " +
                        "based on the user's request. Your responses should be well-structured and informative.";
        }
    }

    private double getTemperatureForContentType(String contentType) {
        switch (contentType != null ? contentType.toLowerCase() : "") {
            case "story":
                return 0.8; // More creative

            case "strategy":
                return 0.4; // More focused and precise

            case "pokedex":
                return 0.6; // Balanced between creative and factual

            case "explanation":
                return 0.3; // More factual and consistent

            default:
                return 0.5; // Default balanced temperature
        }
    }
}