package com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Ability {
    private AbilityDetail ability;
    @JsonProperty("is_hidden")
    private Boolean isHidden;
    private Integer slot;
}