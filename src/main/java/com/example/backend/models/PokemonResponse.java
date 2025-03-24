package com.example.backend.models;

import lombok.Data;
import java.util.List;

@Data
public class PokemonResponse {
    private Integer count;
    private String next;
    private String previous;
    private List<PokemonResult> results;
}