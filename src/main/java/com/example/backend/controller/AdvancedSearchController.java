package com.example.backend.controller;

import com.example.backend.models.pokemon.Pokemon;
import com.example.backend.service.AdvancedSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class AdvancedSearchController {

    private final AdvancedSearchService searchService;

    @Autowired
    public AdvancedSearchController(AdvancedSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/types")
    public Flux<Pokemon> searchByTypes(@RequestParam List<String> types) {
        return searchService.searchPokemonsByTypes(types);
    }

    @GetMapping("/sort")
    public Flux<Pokemon> sortPokemon(
            @RequestParam String attribute,
            @RequestParam(defaultValue = "true") boolean ascending) {
        return searchService.getPokemonSortedByAttribute(attribute, ascending);
    }

    @GetMapping("/random")
    public Mono<Pokemon> getRandomPokemon(
            @RequestParam(defaultValue = "898") int maxId) {
        return searchService.getRandomPokemon(maxId);
    }
}