package com.example.backend.controller;

import com.example.backend.models.recommendation.RecommendationRequest;
import com.example.backend.models.recommendation.RecommendationResult;
import com.example.backend.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/similar")
    public Mono<RecommendationResult> getSimilarPokemon(
            @RequestParam(required = false) String pokemon,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false, defaultValue = "5") Integer limit,
            @RequestParam(required = false) List<String> preferredTypes,
            @RequestParam(required = false, defaultValue = "false") Boolean includeEvolutions,
            @RequestParam(required = false, defaultValue = "balanced") String similarityStrategy) {

        // Build request
        RecommendationRequest request = RecommendationRequest.builder()
                .pokemonName(pokemon)
                .pokemonId(id)
                .limit(limit)
                .preferredTypes(preferredTypes)
                .includeEvolutions(includeEvolutions)
                .similarityStrategy(similarityStrategy)
                .build();

        return recommendationService.findSimilarPokemon(request);
    }

    @PostMapping("/similar")
    public Mono<RecommendationResult> recommendSimilarPokemon(@RequestBody RecommendationRequest request) {
        return recommendationService.findSimilarPokemon(request);
    }
}