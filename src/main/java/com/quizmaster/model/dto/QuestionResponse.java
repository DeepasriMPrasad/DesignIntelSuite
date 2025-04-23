package com.quizmaster.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {
    private String questionId;
    private String text;
    private List<OptionDto> options;
    private int attempts;
    private int maxAttempts;
    private int completedQuestions;
    private int totalQuestions;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDto {
        private String id;
        private String text;
    }
}
