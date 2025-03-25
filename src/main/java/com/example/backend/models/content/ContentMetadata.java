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
public class ContentMetadata {
    private String targetAudience;
    private Integer wordCount;
    private String language;
    private List<String> tags;
    private String format;
    private String tone;
}