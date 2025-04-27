package com.quizmaster.service;

import com.quizmaster.model.QuizResult;

import java.util.List;

/**
 * Interface for exporting and importing quiz results
 */
public interface QuizResultExporter {
    
    /**
     * Export quiz results to external storage (e.g., Excel)
     * 
     * @param results List of quiz results to export
     */
    void exportResults(List<QuizResult> results);
    
    /**
     * Import quiz results from external storage (e.g., Excel)
     * 
     * @return List of imported quiz results
     */
    List<QuizResult> importResults();
}