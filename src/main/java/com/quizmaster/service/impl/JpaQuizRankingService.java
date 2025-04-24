package com.quizmaster.service.impl;

import com.quizmaster.model.QuizResult;
import com.quizmaster.model.dto.RankingResponse;
import com.quizmaster.repository.QuizResultJpaRepository;
import com.quizmaster.service.QuizRankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

    @Autowired
    public JpaQuizRankingService(QuizResultJpaRepository quizResultRepository) {
        this.quizResultRepository = quizResultRepository;
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
        log.debug("Checking if user with I-Number {} has already taken the quiz", iNumber);
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