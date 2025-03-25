package com.example.backend.models.content;

import com.example.backend.models.pokemon.Pokemon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedContent {
    private String id;
    private String title;
    private String content;
    private String contentType;
    private Pokemon featuredPokemon;
    private List<Pokemon> featuredTeam;
    private LocalDateTime generatedDate;
    private String imagePrompt;
    private ContentMetadata metadata;
}