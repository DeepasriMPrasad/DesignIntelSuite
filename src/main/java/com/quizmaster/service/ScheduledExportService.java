package com.quizmaster.service;

import com.quizmaster.model.QuizResult;
import com.quizmaster.util.LoggingUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to periodically export quiz results to Excel
 */
@Service
public class ScheduledExportService {

    private static final Logger log = LoggingUtils.getLogger(ScheduledExportService.class);
    
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
        LoggingUtils.logEvent(log, "ExportStart", "Manual export of quiz results to Excel initiated");
        try {
            List<QuizResult> results = quizRankingService.getAllResults();
            if (results.isEmpty()) {
                LoggingUtils.logEvent(log, "ExportSkipped", "No quiz results to export");
                return 0;
            }
            
            LoggingUtils.logDatabaseOperation(log, "SELECT", "QuizResults", "Retrieved " + results.size() + " results for export");
            quizResultExporter.exportResults(results);
            LoggingUtils.logEvent(log, "ExportSuccess", "Successfully exported " + results.size() + " quiz results to Excel");
            return results.size();
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to export quiz results to Excel", e);
            return -1;
        }
    }
}