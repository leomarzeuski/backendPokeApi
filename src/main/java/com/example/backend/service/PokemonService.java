package com.example.backend.service;

import com.example.backend.models.evolution.EvolutionChain;
import com.example.backend.models.pokemon.Pokemon;
import com.example.backend.models.pokemon.PokemonResponse;
import com.example.backend.models.pokemon.PokemonSpecies;
import com.example.backend.models.pokemon.PokemonType;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PokemonService {

    private final WebClient webClient;

    public PokemonService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://pokeapi.co/api/v2").build();
    }

    public Mono<Pokemon> getPokemonByName(String name) {
        return webClient.get()
                .uri("/pokemon/{name}", name)
                .retrieve()
                .bodyToMono(Pokemon.class);
    }

    public Mono<Pokemon> getPokemonById(Long id) {
        return webClient.get()
                .uri("/pokemon/{id}", id)
                .retrieve()
                .bodyToMono(Pokemon.class);
    }

    public Flux<Pokemon> getAllPokemons(int limit, int offset) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/pokemon")
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build())
                .retrieve()
                .bodyToMono(PokemonResponse.class)
                .flatMapMany(response -> Flux.fromIterable(response.getResults()))
                .flatMap(result -> getPokemonByName(result.getName()));
    }

    public Mono<PokemonResponse> getPokemonList(int limit, int offset) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/pokemon")
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build())
                .retrieve()
                .bodyToMono(PokemonResponse.class);
    }

    public Mono<PokemonSpecies> getPokemonSpeciesByName(String name) {
        return webClient.get()
                .uri("/pokemon-species/{name}", name)
                .retrieve()
                .bodyToMono(PokemonSpecies.class);
    }

    public Mono<PokemonSpecies> getPokemonSpeciesById(Long id) {
        return webClient.get()
                .uri("/pokemon-species/{id}", id)
                .retrieve()
                .bodyToMono(PokemonSpecies.class);
    }

    public Mono<EvolutionChain> getEvolutionChainById(Long id) {
        return webClient.get()
                .uri("/evolution-chain/{id}", id)
                .retrieve()
                .bodyToMono(EvolutionChain.class);
    }

    public Mono<EvolutionChain> getEvolutionChainByUrl(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(EvolutionChain.class);
    }

    public Mono<PokemonType> getTypeByName(String name) {
        return webClient.get()
                .uri("/type/{name}", name)
                .retrieve()
                .bodyToMono(PokemonType.class);
    }

    public Flux<Pokemon> getPokemonsByType(String typeName) {
        return getTypeByName(typeName)
                .flatMapMany(type -> Flux.fromIterable(type.getPokemon()))
                .flatMap(typePokemon -> getPokemonByName(typePokemon.getPokemon().getName()));
    }
}