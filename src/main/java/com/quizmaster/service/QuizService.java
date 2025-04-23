package com.quizmaster.service;

import com.quizmaster.model.dto.*;

public interface QuizService {
    
    /**
     * Start a new quiz session for a user
     * 
     * @param request The start quiz request containing user information
     * @return The response with session ID
     */
    StartQuizResponse startQuiz(StartQuizRequest request);
    
    /**
     * Get a random question for the user
     * 
     * @param sessionId The session ID of the user
     * @return A question with multiple choice options
     */
    QuestionResponse getQuestion(String sessionId);
    
    /**
     * Validate the user's answer to a question
     * 
     * @param request The validate answer request containing session ID, question ID, and answer
     * @return The response indicating if the answer is correct
     */
    ValidateAnswerResponse validateAnswer(ValidateAnswerRequest request);
    
    /**
     * Get the current score for a user's quiz session
     * 
     * @param sessionId The session ID of the user
     * @return The current score and related information
     */
    ScoreResponse getScore(String sessionId);
    
    /**
     * End a quiz session
     * 
     * @param sessionId The session ID of the user
     * @return The final results of the quiz
     */
    EndQuizResponse endQuiz(String sessionId);
}
