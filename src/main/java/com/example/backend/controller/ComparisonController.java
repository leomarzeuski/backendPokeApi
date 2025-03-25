package com.example.backend.controller;

import com.example.backend.models.Pokemon;
import com.example.backend.models.ComparisonRequest;
import com.example.backend.models.PokemonComparison;
import com.example.backend.service.ComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/pokemon/compare")
public class ComparisonController {

    private final ComparisonService comparisonService;

    @Autowired
    public ComparisonController(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @GetMapping
    public Flux<Pokemon> comparePokemons(@RequestParam List<Long> ids) {
        return comparisonService.comparePokemons(ids);
    }

    @GetMapping("/detailed")
    public Mono<PokemonComparison> getDetailedComparison(@RequestParam List<Long> ids) {
        return comparisonService.generateDetailedComparison(ids);
    }

    @PostMapping("/detailed")
    public Mono<PokemonComparison> getDetailedComparisonWithOptions(@RequestBody ComparisonRequest request) {
        return comparisonService.generateDetailedComparison(request.getPokemonIds());
    }
}