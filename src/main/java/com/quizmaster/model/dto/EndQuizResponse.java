package com.quizmaster.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndQuizResponse {
    private String sessionId;
    private String userName;
    private int totalQuestions;
    private int correctAnswers;
    private double percentageScore;
    private long duration; // in seconds
    private String message;
    private int score;
}
