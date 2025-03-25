package com.example.backend.models.pokemon;

import com.example.backend.models.FlavorTextEntry;
import com.example.backend.models.Genus;
import com.example.backend.models.NamedApiResource;
import com.example.backend.models.evolution.EvolutionChainUrl;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PokemonSpecies {
    private Long id;
    private String name;

    @JsonProperty("is_baby")
    private Boolean isBaby;

    @JsonProperty("is_legendary")
    private Boolean isLegendary;

    @JsonProperty("is_mythical")
    private Boolean isMythical;

    @JsonProperty("evolution_chain")
    private EvolutionChainUrl evolutionChain;

    private NamedApiResource color;

    @JsonProperty("flavor_text_entries")
    private List<FlavorTextEntry> flavorTextEntries;

    @JsonProperty("genera")
    private List<Genus> genera;

    @JsonProperty("generation")
    private NamedApiResource generation;

    @JsonProperty("growth_rate")
    private NamedApiResource growthRate;

    @JsonProperty("habitat")
    private NamedApiResource habitat;
}