package com.quizmaster.service;

import com.quizmaster.model.QuizResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to periodically export quiz results to Excel
 */
@Service
@Slf4j
public class ScheduledExportService {

    private final QuizRankingService quizRankingService;
    private final QuizResultExporter quizResultExporter;
    
    @Value("${quizmaster.scheduled-export.enabled:true}")
    private boolean exportEnabled;
    
    @Autowired
    public ScheduledExportService(QuizRankingService quizRankingService, QuizResultExporter quizResultExporter) {
        this.quizRankingService = quizRankingService;
        this.quizResultExporter = quizResultExporter;
    }
    
    /**
     * Scheduled task to export quiz results to Excel every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 300,000 ms = 5 minutes
    public void scheduledExport() {
        if (!exportEnabled) {
            log.debug("Scheduled export is disabled");
            return;
        }
        
        exportResultsNow();
    }
    
    /**
     * Manually trigger an export of quiz results to Excel
     * @return The number of results exported, or -1 if an error occurred
     */
    public int exportResultsNow() {
        log.info("Running manual export of quiz results to Excel");
        try {
            List<QuizResult> results = quizRankingService.getAllResults();
            if (results.isEmpty()) {
                log.info("No quiz results to export");
                return 0;
            }
            
            quizResultExporter.exportResults(results);
            log.info("Successfully exported {} quiz results to Excel", results.size());
            return results.size();
        } catch (Exception e) {
            log.error("Error during export of quiz results", e);
            return -1;
        }
    }
}