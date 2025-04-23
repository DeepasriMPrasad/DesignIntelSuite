package com.quizmaster.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreResponse {
    private String sessionId;
    private String userName;
    private int totalQuestions;
    private int correctAnswers;
    private boolean quizComplete;
    private double percentageScore;
}
