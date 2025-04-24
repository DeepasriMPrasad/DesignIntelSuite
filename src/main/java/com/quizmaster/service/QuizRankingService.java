package com.quizmaster.service;

import com.quizmaster.model.QuizResult;
import com.quizmaster.model.dto.RankingResponse;

import java.io.IOException;
import java.util.List;

public interface QuizRankingService {
    void saveQuizResult(QuizResult result);
    List<QuizResult> getTopResults(int limit);
    List<QuizResult> getAllResults();
    RankingResponse getUserRanking(String userName);
    void clearAllResults() throws IOException;
}