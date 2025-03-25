package com.example.backend.models.evolution;

import com.example.backend.models.ChainLink;

import lombok.Data;

@Data
public class EvolutionChain {
    private Long id;
    private ChainLink chain;
}