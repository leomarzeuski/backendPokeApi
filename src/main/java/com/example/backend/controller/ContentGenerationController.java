package com.example.backend.controller;

import com.example.backend.models.content.GeneratedContent;
import com.example.backend.models.content.GenerationRequest;
import com.example.backend.service.ContentGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/generate")
public class ContentGenerationController {

    private final ContentGenerationService contentGenerationService;

    @Autowired
    public ContentGenerationController(ContentGenerationService contentGenerationService) {
        this.contentGenerationService = contentGenerationService;
    }

    @GetMapping("/story")
    public Mono<GeneratedContent> generateStory(
            @RequestParam String pokemon,
            @RequestParam(required = false) String targetAudience,
            @RequestParam(required = false) String tone,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) Integer maxLength,
            @RequestParam(required = false, defaultValue = "false") Boolean includeImagePrompt) {

        GenerationRequest request = GenerationRequest.builder()
                .contentType("story")
                .pokemonName(pokemon)
                .targetAudience(targetAudience)
                .tone(tone)
                .format(format)
                .maxLength(maxLength)
                .includeImagePrompt(includeImagePrompt)
                .build();

        return contentGenerationService.generateContent(request);
    }

    @GetMapping("/pokedex")
    public Mono<GeneratedContent> generatePokedexEntry(
            @RequestParam String pokemon,
            @RequestParam(required = false) String targetAudience,
            @RequestParam(required = false) String tone,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) Integer maxLength,
            @RequestParam(required = false, defaultValue = "false") Boolean includeImagePrompt) {

        GenerationRequest request = GenerationRequest.builder()
                .contentType("pokedex")
                .pokemonName(pokemon)
                .targetAudience(targetAudience)
                .tone(tone)
                .format(format)
                .maxLength(maxLength)
                .includeImagePrompt(includeImagePrompt)
                .build();

        return contentGenerationService.generateContent(request);
    }

    @GetMapping("/strategy")
    public Mono<GeneratedContent> generateStrategy(
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String pokemon,
            @RequestParam(required = false) String targetAudience,
            @RequestParam(required = false) String tone,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) Integer maxLength,
            @RequestParam(required = false, defaultValue = "false") Boolean includeImagePrompt) {

        GenerationRequest.GenerationRequestBuilder builder = GenerationRequest.builder()
                .contentType("strategy")
                .targetAudience(targetAudience)
                .tone(tone)
                .format(format)
                .maxLength(maxLength)
                .includeImagePrompt(includeImagePrompt);

        // Parse either team or single pokemon
        if (team != null && !team.isEmpty()) {
            String[] teamMembers = team.split(",");
            List<String> pokemonNames = new java.util.ArrayList<>();
            for (String member : teamMembers) {
                pokemonNames.add(member.trim());
            }
            builder.teamMembers(pokemonNames);
        } else if (pokemon != null && !pokemon.isEmpty()) {
            builder.pokemonName(pokemon);
        }

        return contentGenerationService.generateContent(builder.build());
    }

    @PostMapping
    public Mono<GeneratedContent> generateContent(@RequestBody GenerationRequest request) {
        return contentGenerationService.generateContent(request);
    }
}