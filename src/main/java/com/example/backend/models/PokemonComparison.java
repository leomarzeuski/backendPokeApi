package com.example.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PokemonComparison {
    private List<Pokemon> pokemons;
    private Map<Long, TypeEffectiveness> typeEffectiveness;
    private StatComparison statComparison;
    private Map<Long, List<String>> abilities;
    private Map<Long, String> spriteUrls;
}