package com.example.backend.models.recommendation;

import com.example.backend.models.pokemon.Pokemon;
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
public class RecommendationResult {
    private Pokemon basePokemon;
    private List<Pokemon> similarPokemon;
    private Map<Long, String> explanations;
    private List<String> matchReasons;
    private double averageSimilarityScore;
}