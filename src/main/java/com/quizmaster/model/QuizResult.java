package com.quizmaster.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResult {
    private String userName;
    private int score;
    private double percentageScore;
    private int totalQuestions;
    private int correctAnswers;
    private int timeTakenSeconds;
    private LocalDateTime completedAt;

    // Calculated fields - don't store these in the CSV/Excel
    private int rank;
}