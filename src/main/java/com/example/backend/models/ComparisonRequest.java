package com.example.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonRequest {
    private List<Long> pokemonIds;
    private boolean includeTypeEffectiveness;
    private boolean includeStatComparison;
    private boolean includeAbilities;
    private boolean includeSprites;
}