package com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Pokemon {
    private Long id;
    private String name;
    private Integer height;
    private Integer weight;
    private List<PokemonType> types;
    private Sprites sprites;
    private List<Ability> abilities;

    @JsonProperty("base_experience")
    private Integer baseExperience;
}