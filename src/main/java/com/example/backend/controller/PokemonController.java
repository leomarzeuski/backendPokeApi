package com.example.backend.controller;

import com.example.backend.models.Pokemon;
import com.example.backend.models.PokemonResponse;
import com.example.backend.service.PokemonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final PokemonService pokemonService;

    @Autowired
    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping("/{name}")
    public Mono<Pokemon> getPokemonByName(@PathVariable String name) {
        return pokemonService.getPokemonByName(name);
    }

    @GetMapping("/id/{id}")
    public Mono<Pokemon> getPokemonById(@PathVariable Long id) {
        return pokemonService.getPokemonById(id);
    }

    @GetMapping
    public Flux<Pokemon> getAllPokemons(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return pokemonService.getAllPokemons(limit, offset);
    }

    @GetMapping("/list")
    public Mono<PokemonResponse> getPokemonList(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return pokemonService.getPokemonList(limit, offset);
    }
}