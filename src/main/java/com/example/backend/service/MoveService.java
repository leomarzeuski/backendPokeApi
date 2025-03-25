package com.example.backend.service;

import com.example.backend.models.Move;
import com.example.backend.models.PokemonResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class MoveService {

    private final WebClient webClient;

    public MoveService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://pokeapi.co/api/v2").build();
    }

    public Mono<Move> getMoveByName(String name) {
        return webClient.get()
                .uri("/move/{name}", name)
                .retrieve()
                .bodyToMono(Move.class);
    }

    public Mono<Move> getMoveById(Long id) {
        return webClient.get()
                .uri("/move/{id}", id)
                .retrieve()
                .bodyToMono(Move.class);
    }

    public Mono<PokemonResponse> getMoveList(int limit, int offset) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/move")
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build())
                .retrieve()
                .bodyToMono(PokemonResponse.class);
    }
}