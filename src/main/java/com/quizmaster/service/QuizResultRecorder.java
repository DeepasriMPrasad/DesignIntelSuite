package com.quizmaster.service;

import com.quizmaster.model.dto.EndQuizResponse;

/**
 * Service for recording quiz results to persistent storage
 */
public interface QuizResultRecorder {
    
    /**
     * Records a quiz result from the end quiz response
     * 
     * @param sessionId the quiz session ID
     * @param userName the user who took the quiz
     * @param response the end quiz response containing score details
     */
    void recordQuizResult(String sessionId, String userName, EndQuizResponse response);
    
}