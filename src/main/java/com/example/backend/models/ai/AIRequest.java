package com.example.backend.models.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {
    private String prompt;
    private String subject;
    private String contentType;
    private Integer maxLength;
}