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
     * Import quiz results from Excel file on application startup
     */
    @PostConstruct
    public void importResultsOnStartup() {
        if (!importFromExcelOnStartup) {
            log.info("Import from Excel on startup is disabled");
            return;
        }
        
        log.info("Importing quiz results from Excel on startup");
        try {
            List<QuizResult> excelResults = quizResultExporter.importResults();
            if (excelResults.isEmpty()) {
                log.info("No quiz results found in Excel file for import");
                return;
            }
            
            // For each result from Excel, check if it already exists in the database
            for (QuizResult result : excelResults) {
                if (result.getId() != null) {
                    if (!quizResultRepository.existsById(result.getId())) {
                        quizResultRepository.save(result);
                        log.debug("Imported quiz result for user: {} with ID: {}", 
                                result.getUserName(), result.getId());
                    } else {
                        log.debug("Quiz result with ID: {} already exists, skipping import", 
                                result.getId());
                    }
                } else {
                    // If no ID is provided, check by I-Number and userName to avoid duplicates
                    boolean exists = quizResultRepository.findAll().stream()
                            .anyMatch(r -> r.getINumber() != null && 
                                    r.getINumber().equals(result.getINumber()) &&
                                    r.getUserName().equals(result.getUserName()));
                    
                    if (!exists) {
                        quizResultRepository.save(result);
                        log.debug("Imported new quiz result for user: {}", result.getUserName());
                    } else {
                        log.debug("Quiz result for user: {} with I-Number: {} already exists, skipping import", 
                                result.getUserName(), result.getINumber());
                    }
                }
            }
            
            log.info("Successfully imported quiz results from Excel on startup");
        } catch (Exception e) {
            log.error("Error importing quiz results from Excel on startup", e);
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