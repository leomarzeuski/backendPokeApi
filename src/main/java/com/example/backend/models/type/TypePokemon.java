package com.example.backend.models.type;

import com.example.backend.models.NamedApiResource;

import lombok.Data;

@Data
public class TypePokemon {
    private NamedApiResource pokemon;
    private Integer slot;
}