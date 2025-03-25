package com.example.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeEffectiveness {
    private List<String> strongAgainst;
    private List<String> weakAgainst;
    private List<String> resistantTo;
    private List<String> immuneTo;
    private String primaryType;
    private String secondaryType;
}