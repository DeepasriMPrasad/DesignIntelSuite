package com.quizmaster.repository.impl;

import com.quizmaster.model.QuizResult;

import java.io.IOException;
import java.util.List;

public interface QuizResultRepository {
    void saveResult(QuizResult result);
    List<QuizResult> getAllResults();
    void clearAllResults() throws IOException;
}