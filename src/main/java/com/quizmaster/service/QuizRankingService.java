package com.quizmaster.service;

import com.quizmaster.model.QuizResult;
import com.quizmaster.model.dto.RankingResponse;

import java.io.IOException;
import java.util.List;

/**
 * Service for managing quiz rankings and results
 */
public interface QuizRankingService {
    
    /**
     * Gets the top results by score
     * 
     * @param limit maximum number of results to return
     * @return list of quiz results ordered by score
     */
    List<QuizResult> getTopResults(int limit);
    
    /**
     * Gets all results
     * 
     * @return list of all quiz results
     */
    List<QuizResult> getAllResults();
    
    /**
     * Gets ranking information for a specific user
     * 
     * @param userName the user's name
     * @return ranking information for the user
     */
    RankingResponse getUserRanking(String userName);
    
    /**
     * Saves a quiz result
     * 
     * @param quizResult the quiz result to save
     */
    void saveQuizResult(QuizResult quizResult);
    
    /**
     * Clears all quiz results
     * 
     * @throws IOException if an error occurs while clearing results
     */
    void clearAllResults() throws IOException;
    
    /**
     * Checks if a user with the given I-Number has already taken the quiz
     * 
     * @param iNumber the I-Number to check
     * @return true if a user with this I-Number has already taken the quiz, false otherwise
     */
    boolean hasUserWithINumberTakenQuiz(String iNumber);
}