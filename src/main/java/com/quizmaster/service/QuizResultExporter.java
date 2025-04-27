package com.quizmaster.service;

import com.quizmaster.model.QuizResult;
import com.quizmaster.service.impl.ExcelQuizResultExporterImpl.ExportTrigger;

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
     * Export quiz results to external storage with a specific trigger type
     * 
     * @param results List of quiz results to export
     * @param trigger The type of trigger that caused the export
     */
    default void exportResults(List<QuizResult> results, ExportTrigger trigger) {
        // Default implementation falls back to the standard export
        exportResults(results);
    }
}