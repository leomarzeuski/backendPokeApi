package com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EvolutionDetail {
    private NamedApiResource trigger;
    @JsonProperty("min_level")
    private Integer minLevel;
    @JsonProperty("min_happiness")
    private Integer minHappiness;
    @JsonProperty("min_beauty")
    private Integer minBeauty;
    @JsonProperty("min_affection")
    private Integer minAffection;
    @JsonProperty("needs_overworld_rain")
    private Boolean needsOverworldRain;
    @JsonProperty("time_of_day")
    private String timeOfDay;
    private NamedApiResource item;
}