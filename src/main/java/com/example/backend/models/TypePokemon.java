package com.example.backend.models;

import lombok.Data;

@Data
public class TypePokemon {
    private NamedApiResource pokemon;
    private Integer slot;
}