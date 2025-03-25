package com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TypeRelations {
    @JsonProperty("double_damage_from")
    private List<NamedApiResource> doubleDamageFrom;
    @JsonProperty("double_damage_to")
    private List<NamedApiResource> doubleDamageTo;
    @JsonProperty("half_damage_from")
    private List<NamedApiResource> halfDamageFrom;
    @JsonProperty("half_damage_to")
    private List<NamedApiResource> halfDamageTo;
    @JsonProperty("no_damage_from")
    private List<NamedApiResource> noDamageFrom;
    @JsonProperty("no_damage_to")
    private List<NamedApiResource> noDamageTo;
}