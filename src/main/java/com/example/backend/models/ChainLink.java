package com.example.backend.models;

import com.example.backend.models.evolution.EvolutionDetail;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ChainLink {
    @JsonProperty("is_baby")
    private Boolean isBaby;
    private NamedApiResource species;
    @JsonProperty("evolution_details")
    private List<EvolutionDetail> evolutionDetails;
    @JsonProperty("evolves_to")
    private List<ChainLink> evolvesTo;
}