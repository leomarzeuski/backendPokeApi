package com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Sprites {
    @JsonProperty("front_default")
    private String frontDefault;

    @JsonProperty("back_default")
    private String backDefault;

    @JsonProperty("front_shiny")
    private String frontShiny;

    @JsonProperty("back_shiny")
    private String backShiny;
}