package com.example.backend.models.recommendation;

import com.example.backend.models.pokemon.Pokemon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarityScore {
    private Pokemon pokemon;
    private double score;
    private Map<String, Double> scoreBreakdown;
}