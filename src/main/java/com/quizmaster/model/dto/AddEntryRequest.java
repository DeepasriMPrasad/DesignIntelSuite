package com.quizmaster.model.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding a new quiz result entry through the admin interface
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddEntryRequest {
    
    @NotBlank(message = "User name is required")
    @Size(min = 2, max = 50, message = "User name must be between 2 and 50 characters")
    private String userName;
    
    @NotBlank(message = "I-Number is required")
    @Size(max = 20, message = "I-Number cannot exceed 20 characters")
    private String iNumber;
    
    @NotNull(message = "Correct answers is required")
    @Min(value = 0, message = "Correct answers cannot be negative")
    @Max(value = 5, message = "Correct answers cannot exceed 5")
    private Integer correctAnswers;
    
    @Builder.Default
    private Integer totalQuestions = 5;
    
    @Builder.Default
    private Integer percentageScore = 0;
    
    @Builder.Default
    private Integer timeTakenSeconds = 60;
    
    @Builder.Default
    private LocalDateTime completedAt = LocalDateTime.now();
    
    /**
     * Calculates the percentage score based on correct answers and total questions
     * This is called automatically when setting correctAnswers or totalQuestions
     */
    public void calculatePercentageScore() {
        if (totalQuestions != null && totalQuestions > 0 && correctAnswers != null) {
            this.percentageScore = (int) Math.round((double) correctAnswers / totalQuestions * 100);
        } else {
            this.percentageScore = 0;
        }
    }
    
    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
        calculatePercentageScore();
    }
    
    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
        calculatePercentageScore();
    }
}