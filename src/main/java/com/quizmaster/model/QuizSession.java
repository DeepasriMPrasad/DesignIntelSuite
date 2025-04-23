package com.quizmaster.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSession {
    private String sessionId;
    private String userName;
    private List<Question> questions;
    private String currentQuestionId;
    private int currentAttempts;
    private Set<String> completedQuestions;
    private int correctAnswers;
    private boolean active;
    private Date startTime;
    private Date endTime;
}
