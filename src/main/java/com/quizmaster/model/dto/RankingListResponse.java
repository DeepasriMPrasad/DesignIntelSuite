package com.quizmaster.model.dto;

import com.quizmaster.model.QuizResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingListResponse {
    private List<QuizResult> rankings;
    private int totalParticipants;
    private LocalDateTime generatedAt;
}