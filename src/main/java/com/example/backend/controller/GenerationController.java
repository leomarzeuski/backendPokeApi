package com.example.backend.controller;

import com.example.backend.models.PokemonResponse;
import com.example.backend.service.GenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/generations")
public class GenerationController {

    private final GenerationService generationService;

    @Autowired
    public GenerationController(GenerationService generationService) {
        this.generationService = generationService;
    }

    @GetMapping("/{name}")
    public Mono<Object> getGenerationByName(@PathVariable String name) {
        return generationService.getGenerationByName(name);
    }

    @GetMapping
    public Mono<PokemonResponse> getGenerationList() {
        return generationService.getGenerationList();
    }
}