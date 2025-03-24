package com.example.backend.service;

import com.example.backend.models.Pokemon;
import com.example.backend.models.PokemonResponse;
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
}