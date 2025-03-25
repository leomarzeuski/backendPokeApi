package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.models.pokemon.Pokemon;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AdvancedSearchService {

    private final PokemonService pokemonService;

    @Autowired
    public AdvancedSearchService(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    public Flux<Pokemon> searchPokemonsByTypes(List<String> types) {
        if (types.size() == 1) {
            return pokemonService.getPokemonsByType(types.get(0));
        } else {
            return pokemonService.getPokemonsByType(types.get(0))
                    .filter(pokemon -> {
                        List<String> pokemonTypes = pokemon.getTypes().stream()
                                .map(type -> type.getType().getName())
                                .toList();
                        return types.stream().allMatch(pokemonTypes::contains);
                    });
        }
    }

    public Flux<Pokemon> getPokemonSortedByAttribute(String attribute, boolean ascending) {
        return pokemonService.getAllPokemons(20, 0)
                .sort((p1, p2) -> {
                    int result = 0;
                    switch (attribute) {
                        case "id":
                            result = p1.getId().compareTo(p2.getId());
                            break;
                        case "name":
                            result = p1.getName().compareTo(p2.getName());
                            break;
                        case "height":
                            result = p1.getHeight().compareTo(p2.getHeight());
                            break;
                        case "weight":
                            result = p1.getWeight().compareTo(p2.getWeight());
                            break;
                        case "base_experience":
                            result = p1.getBaseExperience().compareTo(p2.getBaseExperience());
                            break;
                    }
                    return ascending ? result : -result;
                });
    }

    public Mono<Pokemon> getRandomPokemon(int maxId) {
        long randomId = (long) (Math.random() * maxId) + 1;
        return pokemonService.getPokemonById(randomId);
    }
}