package com.quizmaster.service.impl;

import com.quizmaster.model.QuizResult;
import com.quizmaster.model.dto.RankingResponse;
import com.quizmaster.repository.QuizResultJpaRepository;
import com.quizmaster.service.QuizRankingService;
import com.quizmaster.service.QuizResultExporter;
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
     * 1. Create a backup of the Excel file
     * 2. Clear the database
     * 3. Import all data from Excel
     * 4. If Excel import fails, keep existing DB data
     */
    @PostConstruct
    public void importResultsOnStartup() {
        if (!importFromExcelOnStartup) {
            log.info("Import from Excel on startup is disabled");
            return;
        }
        
        log.info("Synchronizing quiz results with Excel on startup");
        
        try {
            // Attempt to create backup of Excel file first
            quizResultExporter.createBackup();
            
            // Load results from Excel file
            List<QuizResult> excelResults = quizResultExporter.importResults();
            log.info("Found {} quiz results in Excel file for import", excelResults.size());
            
            if (excelResults.isEmpty()) {
                log.info("No quiz results found in Excel file for import, keeping database records");
                // Export existing database records to Excel to ensure sync
                List<QuizResult> dbResults = quizResultRepository.findAll();
                if (!dbResults.isEmpty()) {
                    log.info("Exporting {} existing database records to Excel", dbResults.size());
                    quizResultExporter.exportResults(dbResults);
                }
                return;
            }
            
            // Clear the database before importing from Excel to avoid duplicates
            long existingRecords = quizResultRepository.count();
            if (existingRecords > 0) {
                log.info("Clearing {} existing records from database before import", existingRecords);
                quizResultRepository.deleteAll();
            }
            
            // Import all results from Excel
            int importedCount = 0;
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
            log.info("Total records in database after import: {}", quizResultRepository.count());
            
            // Register shutdown hook to ensure data is exported on application shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Application shutting down, exporting quiz results to Excel");
                try {
                    List<QuizResult> finalResults = quizResultRepository.findAll();
                    quizResultExporter.exportResults(finalResults);
                    log.info("Successfully exported {} quiz results to Excel on shutdown", finalResults.size());
                } catch (Exception e) {
                    log.error("Error exporting quiz results to Excel on shutdown", e);
                }
            }));
            
        } catch (Exception e) {
            log.error("Error during Excel/DB synchronization on startup", e);
            log.warn("Continuing with existing database records since Excel synchronization failed");
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
}