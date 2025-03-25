package com.example.backend.models.recommendation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    private String pokemonName;
    private Long pokemonId;
    private Integer limit;
    private List<String> preferredTypes;
    private Boolean includeEvolutions;
    private String similarityStrategy;
}