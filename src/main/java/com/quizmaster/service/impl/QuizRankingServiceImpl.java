package com.quizmaster.service.impl;

import com.quizmaster.model.QuizResult;
import com.quizmaster.model.dto.RankingResponse;
import com.quizmaster.repository.impl.QuizResultRepository;
import com.quizmaster.service.QuizRankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Qualifier("fileQuizRankingService")
public class QuizRankingServiceImpl implements QuizRankingService {

    private final QuizResultRepository quizResultRepository;

    @Autowired
    public QuizRankingServiceImpl(QuizResultRepository quizResultRepository) {
        this.quizResultRepository = quizResultRepository;
    }

    @Override
    public void saveQuizResult(QuizResult result) {
        quizResultRepository.saveResult(result);
    }

    @Override
    public List<QuizResult> getTopResults(int limit) {
        return getRankedResults().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuizResult> getAllResults() {
        return getRankedResults();
    }

    @Override
    public RankingResponse getUserRanking(String userName) {
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
                .bestCompletionTimeSeconds(bestResult.getTimeTakenSeconds())
                .totalAttempts(userResults.size())
                .lastCompletedAt(bestResult.getCompletedAt())
                .totalParticipants(rankedResults.size())
                .build();
    }

    @Override
    public void clearAllResults() throws IOException {
        quizResultRepository.clearAllResults();
    }
    
    @Override
    public void deleteQuizResult(Long id) throws IOException {
        // Get all results
        List<QuizResult> results = quizResultRepository.getAllResults();
        
        // Find and remove the result with the given ID
        boolean removed = results.removeIf(result -> result.getId() != null && result.getId().equals(id));
        
        if (removed) {
            // Save the updated list back
            quizResultRepository.saveAllResults(results);
        }
    }
    
    @Override
    public boolean hasUserWithINumberTakenQuiz(String iNumber) {
        // For file-based implementation, we need to check all results
        if (iNumber == null || iNumber.trim().isEmpty()) {
            return false;
        }
        
        // Check if any result has this iNumber
        return quizResultRepository.getAllResults().stream()
                .anyMatch(result -> iNumber.trim().equals(result.getINumber()));
    }

    private List<QuizResult> getRankedResults() {
        List<QuizResult> results = quizResultRepository.getAllResults();

        // Sort by percentage score (desc), then by time taken (asc) if scores are equal
        results.sort(
                Comparator.comparing(QuizResult::getPercentageScore, Comparator.reverseOrder())
                        .thenComparing(QuizResult::getTimeTakenSeconds)
        );

        // Assign ranks
        AtomicInteger rank = new AtomicInteger(1);
        results.forEach(result -> result.setRank(rank.getAndIncrement()));

        return results;
    }
}