package com.quizmaster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quiz_results")
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String sessionId;
    private String userName;
    private String iNumber;
    private int score;
    private double percentageScore;
    private int totalQuestions;
    private int correctAnswers;
    private int timeTakenSeconds;
    
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime completedAt;

    // Calculated fields - not persisted in the database
    @Transient
    private int rank;
}