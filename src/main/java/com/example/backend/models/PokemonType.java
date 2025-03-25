package com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PokemonType {
    private Integer slot;
    private Type type;

    private Long id;
    private String name;
    @JsonProperty("damage_relations")
    private TypeRelations damageRelations;
    @JsonProperty("pokemon")
    private List<TypePokemon> pokemon;
}