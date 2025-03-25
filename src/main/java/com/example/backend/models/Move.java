package com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Move {
    private Long id;
    private String name;
    private Integer accuracy;
    @JsonProperty("effect_chance")
    private Integer effectChance;
    private Integer pp;
    private Integer priority;
    private Integer power;
    @JsonProperty("damage_class")
    private NamedApiResource damageClass;
    @JsonProperty("effect_entries")
    private List<VerboseEffect> effectEntries;
    private MoveMetaData meta;
    private NamedApiResource type;
    private NamedApiResource target;
}