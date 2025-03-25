package com.example.backend.models;

import lombok.Data;

@Data
public class VerboseEffect {
    private String effect;
    private String shortEffect;
    private NamedApiResource language;
}