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
    private int rank;
    private double percentageScore;
    private int score;
    private int totalQuestions;
    private int bestCompletionTimeSeconds;
    private int totalAttempts;
    private LocalDateTime lastCompletedAt;
    private int totalParticipants;
    private String message;
}