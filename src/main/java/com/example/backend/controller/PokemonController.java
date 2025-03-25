package com.example.backend.controller;

import com.example.backend.models.*;
import com.example.backend.models.evolution.EvolutionChain;
import com.example.backend.models.pokemon.Pokemon;
import com.example.backend.models.pokemon.PokemonResponse;
import com.example.backend.models.pokemon.PokemonSpecies;
import com.example.backend.service.PokemonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pokemon")
@Tag(name = "Pokemon", description = "API para operações relacionadas a Pokémon")
public class PokemonController {
    private final PokemonService pokemonService;

    @Autowired
    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping("/{name}")
    @Operation(summary = "Buscar Pokémon por nome", description = "Retorna informações detalhadas de um Pokémon específico pelo nome")
    public Mono<Pokemon> getPokemonByName(@Parameter(description = "Nome do Pokémon") @PathVariable String name) {
        return pokemonService.getPokemonByName(name.toLowerCase());
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Buscar Pokémon por ID", description = "Retorna informações detalhadas de um Pokémon específico pelo ID")
    public Mono<Pokemon> getPokemonById(@Parameter(description = "ID do Pokémon") @PathVariable Long id) {
        return pokemonService.getPokemonById(id);
    }

    @GetMapping
    @Operation(summary = "Listar todos os Pokémon com detalhes", description = "Retorna uma lista paginada de Pokémon com todos os detalhes")
    public Flux<Pokemon> getAllPokemons(
            @Parameter(description = "Número máximo de resultados") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Posição inicial dos resultados") @RequestParam(defaultValue = "0") int offset) {
        return pokemonService.getAllPokemons(limit, offset);
    }

    @GetMapping("/list")
    @Operation(summary = "Listar Pokémon básicos", description = "Retorna uma lista paginada de Pokémon com informações básicas (nome e URL)")
    public Mono<PokemonResponse> getPokemonList(
            @Parameter(description = "Número máximo de resultados") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Posição inicial dos resultados") @RequestParam(defaultValue = "0") int offset) {
        return pokemonService.getPokemonList(limit, offset);
    }

    @GetMapping("/{name}/species")
    @Operation(summary = "Buscar espécie de Pokémon por nome", description = "Retorna informações da espécie de um Pokémon específico pelo nome")
    public Mono<PokemonSpecies> getPokemonSpeciesByName(
            @Parameter(description = "Nome do Pokémon") @PathVariable String name) {
        return pokemonService.getPokemonSpeciesByName(name.toLowerCase());
    }

    @GetMapping("/id/{id}/species")
    @Operation(summary = "Buscar espécie de Pokémon por ID", description = "Retorna informações da espécie de um Pokémon específico pelo ID")
    public Mono<PokemonSpecies> getPokemonSpeciesById(@Parameter(description = "ID do Pokémon") @PathVariable Long id) {
        return pokemonService.getPokemonSpeciesById(id);
    }

    @GetMapping("/{name}/evolution-chain")
    public Mono<EvolutionChain> getEvolutionChain(@PathVariable String name) {
        return pokemonService.getPokemonSpeciesByName(name.toLowerCase())
                .flatMap(species -> {
                    if (species.getEvolutionChain() == null) {
                        return Mono.error(new RuntimeException("Cadeia evolutiva não encontrada para: " + name));
                    }

                    String url = species.getEvolutionChain().getUrl();

                    if (url == null || url.isEmpty()) {
                        return Mono.error(new RuntimeException("URL da cadeia evolutiva não encontrada para: " + name));
                    }

                    return pokemonService.getEvolutionChainByUrl(url);
                });
    }

    @GetMapping("/type/{typeName}")
    @Operation(summary = "Buscar Pokémon por tipo", description = "Retorna uma lista de Pokémon que pertencem a um tipo específico")
    public Flux<Pokemon> getPokemonsByType(@Parameter(description = "Nome do tipo") @PathVariable String typeName) {
        return pokemonService.getPokemonsByType(typeName.toLowerCase());
    }
}