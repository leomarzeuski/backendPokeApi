package com.example.backend.service;

import com.example.backend.models.PokemonResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GenerationService {

    private final WebClient webClient;

    public GenerationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://pokeapi.co/api/v2").build();
    }

    public Mono<Object> getGenerationByName(String name) {
        return webClient.get()
                .uri("/generation/{name}", name)
                .retrieve()
                .bodyToMono(Object.class);
    }

    public Mono<PokemonResponse> getGenerationList() {
        return webClient.get()
                .uri("/generation")
                .retrieve()
                .bodyToMono(PokemonResponse.class);
    }
}