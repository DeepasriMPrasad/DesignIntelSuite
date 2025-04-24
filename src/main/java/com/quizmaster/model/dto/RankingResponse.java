package com.quizmaster.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponse {
    private String userName;
    private String iNumber;
    private int rank;
    private int score;
    private double percentageScore;
    private int totalQuestions;
    private int correctAnswers;
    private int timeTakenSeconds;
    private LocalDateTime completedAt;
    private int totalParticipants;
    
    // Additional fields used by QuizRankingServiceImpl
    private String message;
    private int bestCompletionTimeSeconds;
    private int totalAttempts;
    private LocalDateTime lastCompletedAt;
}