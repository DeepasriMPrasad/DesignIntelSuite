package com.quizmaster.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("userName")
    private String userName;
    
    @NotBlank(message = "I-Number cannot be empty")
    @JsonProperty("iNumber")
    private String iNumber;
}
