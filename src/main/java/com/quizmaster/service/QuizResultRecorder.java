package com.quizmaster.service;

import com.quizmaster.model.dto.EndQuizResponse;

public interface QuizResultRecorder {
    void recordQuizResult(String sessionId, String userName, EndQuizResponse endQuizResponse);
}