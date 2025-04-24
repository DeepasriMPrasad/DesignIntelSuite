package com.quizmaster.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartQuizRequest {
    
    @NotBlank(message = "User name cannot be empty")
    private String userName;
    
    @NotBlank(message = "I-Number cannot be empty")
    private String iNumber;
}
