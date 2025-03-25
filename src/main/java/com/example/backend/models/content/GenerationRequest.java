package com.example.backend.models.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationRequest {
    private String contentType;
    private String pokemonName;
    private Long pokemonId;
    private List<String> teamMembers;
    private List<Long> teamIds;
    private String targetAudience;
    private String language;
    private String tone;
    private Integer maxLength;
    private String format;
    private Boolean includeImagePrompt;
}