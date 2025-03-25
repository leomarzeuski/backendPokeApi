package com.example.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatComparison {
    private Map<Long, Integer> height;
    private Map<Long, Integer> weight;
    private Map<Long, Integer> baseExperience;
    private Map<String, Long> highest;
    private Map<String, Long> lowest;
    private Map<String, Map<Long, Double>> normalizedValues;
}