package com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FlavorTextEntry {
    @JsonProperty("flavor_text")
    private String flavorText;
    private NamedApiResource language;
    private NamedApiResource version;
}