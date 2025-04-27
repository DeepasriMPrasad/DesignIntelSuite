package com.quizmaster.service;

import com.quizmaster.model.QuizResult;

import java.io.IOException;
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
    
    /**
     * Create a backup of the results file before performing operations
     * 
     * @return true if backup was created successfully, false otherwise
     * @throws IOException if an I/O error occurs
     */
    boolean createBackup() throws IOException;
}