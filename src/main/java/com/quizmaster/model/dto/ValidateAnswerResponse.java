package com.quizmaster.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateAnswerResponse {
    private String questionId;
    private boolean correct;
    private String message;
    private int attempts;
    private int maxAttempts;
    private Integer remainingAttempts;
    private String correctAnswerId;
    private Integer remainingQuestions;
}
