package com.quizmaster.service;

import java.io.IOException;
import java.util.List;

import com.quizmaster.model.Question;

/**
 * Service interface for managing quiz questions
 */
public interface QuestionEditorService {
    
    /**
     * Retrieves all questions available in the system
     * 
     * @return A list of all questions
     */
    List<Question> getAllQuestions();
    
    /**
     * Retrieves a specific question by its ID
     * 
     * @param id The ID of the question to retrieve
     * @return The question with the specified ID
     */
    Question getQuestionById(String id);
    
    /**
     * Updates an existing question
     * 
     * @param question The updated question data
     * @return The updated question
     * @throws IOException If an error occurs while saving the changes
     */
    Question updateQuestion(Question question) throws IOException;
    
    /**
     * Adds a new question to the system
     * 
     * @param question The new question to add
     * @return The added question with its assigned ID
     * @throws IOException If an error occurs while adding the question
     */
    Question addQuestion(Question question) throws IOException;
    
    /**
     * Deletes a question from the system
     * 
     * @param id The ID of the question to delete
     * @throws IOException If an error occurs while deleting the question
     */
    void deleteQuestion(String id) throws IOException;
    
    /**
     * Saves all questions to the Excel file
     * 
     * @throws IOException If an error occurs while saving to Excel
     */
    void saveQuestionsToExcel() throws IOException;
}