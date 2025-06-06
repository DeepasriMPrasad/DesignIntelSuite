package com.quizmaster.service.impl;

import com.quizmaster.model.QuizResult;
import com.quizmaster.model.dto.RankingResponse;
import com.quizmaster.repository.QuizResultJpaRepository;
import com.quizmaster.service.QuizRankingService;
import com.quizmaster.service.QuizResultExporter;
import com.quizmaster.service.impl.ExcelQuizResultExporterImpl.ExportTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * JPA implementation of QuizRankingService
 */
@Service
@Slf4j
@Primary
@Qualifier("jpaQuizRankingService")
public class JpaQuizRankingService implements QuizRankingService {

    private final QuizResultJpaRepository quizResultRepository;
    private final QuizResultExporter quizResultExporter;
    
    @Value("${quizmaster.import-from-excel-on-startup:true}")
    private boolean importFromExcelOnStartup;

    @Autowired
    public JpaQuizRankingService(QuizResultJpaRepository quizResultRepository, 
                                 QuizResultExporter quizResultExporter) {
        this.quizResultRepository = quizResultRepository;
        this.quizResultExporter = quizResultExporter;
    }
    
    /**
     * Import quiz results from Excel file on application startup.
     * The strategy is to prioritize Excel data over DB data during startup:
     * 1. First export existing DB data to db_results.xlsx (data safety)
     * 2. Import all data from Excel
     * 3. If Excel import fails, keep existing DB data
     * 
     * This is done synchronously to ensure data integrity before the application starts accepting requests
     */
    @PostConstruct
    public void importResultsOnStartup() {
        if (!importFromExcelOnStartup) {
            log.info("Import from Excel on startup is disabled");
            return;
        }
        
        log.info("Starting Excel synchronization (synchronous)");
        
        try {
            log.info("Synchronizing quiz results with Excel on startup");
            
            // First, export existing DB data to db_results.xlsx before we potentially overwrite it
            exportExistingDbToSpecialFile();
            
            // Load results from Excel file
            List<QuizResult> excelResults = quizResultExporter.importResults();
            log.info("Found {} quiz results in Excel file for import", excelResults.size());
            
            if (excelResults.isEmpty()) {
                log.info("No quiz results found in Excel file for import, keeping database records");
                // Export existing database records to Excel to ensure sync
                List<QuizResult> dbResults = quizResultRepository.findAll();
                if (!dbResults.isEmpty()) {
                    log.info("Exporting {} existing database records to Excel", dbResults.size());
                    quizResultExporter.exportResults(dbResults, ExportTrigger.STARTUP);
                }
                return;
            }
            
            // Check if all Excel entries already exist in the database by I-Number
            List<QuizResult> dbResults = quizResultRepository.findAll();
            boolean hasNewEntries = false;
            boolean countMismatch = excelResults.size() != dbResults.size();
            
            if (countMismatch) {
                log.info("Count mismatch: Excel has {} entries, database has {} entries", 
                         excelResults.size(), dbResults.size());
            }
            
            // Collect all I-Numbers from database
            List<String> dbINumbers = dbResults.stream()
                .filter(result -> result.getINumber() != null && !result.getINumber().isEmpty())
                .map(QuizResult::getINumber)
                .collect(Collectors.toList());
                
            log.info("Found {} I-Numbers in database", dbINumbers.size());
            
            // Check if any Excel entry has an I-Number not present in the database
            for (QuizResult excelResult : excelResults) {
                String iNumber = excelResult.getINumber();
                if (iNumber != null && !iNumber.isEmpty() && !dbINumbers.contains(iNumber)) {
                    log.info("Found new I-Number in Excel that is not in database: {}", iNumber);
                    hasNewEntries = true;
                    break;
                }
            }
            
            // Declare importedCount outside the if-else block
            int importedCount = 0;
            
            if (hasNewEntries || countMismatch) {
                // Clear the database if new entries were found or if counts don't match
                String reason = hasNewEntries ? "new I-Numbers found" : "count mismatch";
                log.info("Clearing database before importing due to: {}", reason);
                quizResultRepository.deleteAll();
                
                // Import all results from Excel
                for (QuizResult result : excelResults) {
                    try {
                        QuizResult saved = quizResultRepository.save(result);
                        importedCount++;
                        log.debug("Imported quiz result for user: {} with ID: {}", 
                                result.getUserName(), saved.getId());
                    } catch (Exception e) {
                        log.error("Failed to import result for user: {}", result.getUserName(), e);
                    }
                }
                log.info("Excel import complete: {} imported from Excel", importedCount);
            } else {
                log.info("All I-Numbers in Excel file already exist in database and counts match - skipping import");
                // Export existing database records to Excel to ensure sync
                if (!dbResults.isEmpty()) {
                    log.info("Keeping existing {} database records and ensuring Excel is in sync", dbResults.size());
                    quizResultExporter.exportResults(dbResults, ExportTrigger.STARTUP);
                }
            }
            
            log.info("Total records in database after import: {}", quizResultRepository.count());
            
        } catch (Exception e) {
            log.error("Error during Excel/DB synchronization on startup", e);
            log.warn("Continuing with existing database records since Excel synchronization failed");
        }
        
        // Register a shutdown hook to ensure results are saved on application exit
        // This is only registered once when the service is created
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    log.info("Application shutting down, exporting quiz results to Excel");
                    try {
                        List<QuizResult> finalResults = quizResultRepository.findAll();
                        quizResultExporter.exportResults(finalResults, ExportTrigger.SHUTDOWN);
                        log.info("Successfully exported {} quiz results to Excel on shutdown", finalResults.size());
                    } catch (Exception dbException) {
                        // Database might already be closed during shutdown
                        if (dbException.toString().contains("Database is already closed")) {
                            log.warn("Database already closed during shutdown hook execution, skipping export");
                        } else {
                            throw dbException;
                        }
                    }
                } catch (Exception e) {
                    log.error("Error exporting quiz results to Excel on shutdown", e);
                }
            }));
        } catch (IllegalStateException e) {
            // This happens if shutdown is already in progress, which is fine to ignore
            log.debug("Could not add shutdown hook - shutdown may already be in progress");
        }
    }

    @Override
    public List<QuizResult> getTopResults(int limit) {
        log.debug("Getting top {} quiz results", limit);
        return getRankedResults().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuizResult> getAllResults() {
        log.debug("Getting all quiz results");
        return getRankedResults();
    }

    @Override
    public RankingResponse getUserRanking(String userName) {
        log.debug("Getting ranking for user: {}", userName);
        
        List<QuizResult> rankedResults = getRankedResults();
        
        // Find all results for the user
        List<QuizResult> userResults = rankedResults.stream()
                .filter(r -> r.getUserName().equals(userName))
                .collect(Collectors.toList());
        
        // Find the best result (highest rank/lowest rank number)
        QuizResult bestResult = userResults.stream()
                .min(Comparator.comparing(QuizResult::getRank))
                .orElse(null);
        
        if (bestResult == null) {
            return RankingResponse.builder()
                    .userName(userName)
                    .message("No quiz results found for this user")
                    .rank(0)
                    .totalParticipants(rankedResults.size())
                    .build();
        }
        
        return RankingResponse.builder()
                .userName(userName)
                .rank(bestResult.getRank())
                .percentageScore(bestResult.getPercentageScore())
                .score(bestResult.getScore())
                .totalQuestions(bestResult.getTotalQuestions())
                .correctAnswers(bestResult.getCorrectAnswers())
                .bestCompletionTimeSeconds(bestResult.getTimeTakenSeconds())
                .timeTakenSeconds(bestResult.getTimeTakenSeconds())
                .totalAttempts(userResults.size())
                .lastCompletedAt(bestResult.getCompletedAt())
                .completedAt(bestResult.getCompletedAt())
                .totalParticipants(rankedResults.size())
                .build();
    }

    @Override
    public void saveQuizResult(QuizResult quizResult) {
        log.debug("Saving quiz result for user: {}", quizResult.getUserName());
        quizResultRepository.save(quizResult);
    }

    @Override
    public void clearAllResults() throws IOException {
        log.debug("Clearing all quiz results");
        quizResultRepository.deleteAll();
    }
    
    @Override
    public void deleteQuizResult(Long id) throws IOException {
        log.debug("Deleting quiz result with ID: {}", id);
        quizResultRepository.deleteById(id);
    }
    
    @Override
    public boolean hasUserWithINumberTakenQuiz(String iNumber) {
        log.debug("Checking if user with Identification Number {} has already taken the quiz", iNumber);
        if (iNumber == null || iNumber.trim().isEmpty()) {
            return false;
        }
        return quizResultRepository.existsByiNumber(iNumber.trim());
    }
    
    private List<QuizResult> getRankedResults() {
        List<QuizResult> results = quizResultRepository.findAll(
                Sort.by(Sort.Direction.DESC, "percentageScore")
                    .and(Sort.by(Sort.Direction.ASC, "timeTakenSeconds"))
        );
        
        // Assign ranks
        AtomicInteger rank = new AtomicInteger(1);
        results.forEach(result -> result.setRank(rank.getAndIncrement()));
        
        return results;
    }
    
    /**
     * Exports the existing database content to a special db_results.xlsx file
     * before loading data from results.xlsx
     * This ensures we don't lose any database data during the synchronization process
     */
    private void exportExistingDbToSpecialFile() {
        try {
            List<QuizResult> existingDbResults = quizResultRepository.findAll();
            if (existingDbResults == null || existingDbResults.isEmpty()) {
                log.info("No existing database results to export to db_results.xlsx before sync");
                return;
            }
            
            log.info("Exporting {} existing database results to db_results.xlsx before Excel sync", existingDbResults.size());
            
            // Export to special db results file using the QuizResultRecorder 
            if (quizResultExporter instanceof ExcelQuizResultExporterImpl) {
                // If it's the expected exporter type
                try {
                    // Call specialized method for exporting to db_results.xlsx
                    ((ExcelQuizResultExporterImpl) quizResultExporter)
                        .exportToSpecialFile(existingDbResults, "db_results.xlsx");
                    log.info("Successfully exported {} existing database records to db_results.xlsx before sync",
                           existingDbResults.size());
                } catch (Exception e) {
                    log.error("Error exporting existing database to db_results.xlsx: {}", e.getMessage(), e);
                }
            } else {
                // Otherwise fall back to standard export
                log.info("Special export not available, using standard export");
                quizResultExporter.exportResults(existingDbResults, ExportTrigger.STARTUP);
            }
        } catch (Exception e) {
            log.error("Failed to export existing database to db_results.xlsx: {}", e.getMessage(), e);
            log.warn("Continuing with Excel sync despite db_results.xlsx export failure");
        }
    }
}