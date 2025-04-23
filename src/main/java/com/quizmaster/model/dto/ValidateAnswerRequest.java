package com.quizmaster.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateAnswerRequest {
    
    @NotBlank(message = "Session ID cannot be empty")
    private String sessionId;
    
    @NotBlank(message = "Question ID cannot be empty")
    private String questionId;
    
    @NotBlank(message = "Answer ID cannot be empty")
    private String answerId;
}
