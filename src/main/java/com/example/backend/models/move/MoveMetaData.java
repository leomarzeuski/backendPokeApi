package com.example.backend.models.move;

import com.example.backend.models.NamedApiResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MoveMetaData {
    private NamedApiResource category;
    private Integer drain;
    private Integer healing;
    @JsonProperty("max_hits")
    private Integer maxHits;
    @JsonProperty("max_turns")
    private Integer maxTurns;
    @JsonProperty("min_hits")
    private Integer minHits;
    @JsonProperty("min_turns")
    private Integer minTurns;
    private Integer statChance;
}