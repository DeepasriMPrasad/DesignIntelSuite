package com.quizmaster.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartQuizResponse {
    private String sessionId;
    private String message;
    private String userName;
    private String iNumber;
    private int maxQuestions;
}
