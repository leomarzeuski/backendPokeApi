package com.example.backend.models.comparison;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

import com.example.backend.models.pokemon.Pokemon;
import com.example.backend.models.type.TypeEffectiveness;

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