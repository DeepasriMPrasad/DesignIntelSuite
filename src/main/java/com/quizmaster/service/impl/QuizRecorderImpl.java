package com.quizmaster.service.impl;

import com.quizmaster.exception.QuizException;
import com.quizmaster.model.QuizResult;
import com.quizmaster.model.dto.EndQuizResponse;
import com.quizmaster.service.QuizRankingService;
import com.quizmaster.service.QuizResultRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of the QuizResultRecorder interface that records quiz results
 * to be used in the ranking system.
 */
@Service
class QuizResultRecorderImpl implements QuizResultRecorder {

    private static final Logger logger = LoggerFactory.getLogger(QuizResultRecorderImpl.class);

    private final QuizRankingService quizRankingService;

    @Autowired
    public QuizResultRecorderImpl(QuizRankingService quizRankingService) {
        this.quizRankingService = quizRankingService;
    }

    @Override
    public void recordQuizResult(String sessionId, String userName, EndQuizResponse endQuizResponse) {
        try {
            if (endQuizResponse == null) {
                logger.warn("Cannot record quiz result: EndQuizResponse is null for session {}", sessionId);
                return;
            }

            if (userName == null || userName.trim().isEmpty()) {
                logger.warn("Cannot record quiz result: userName is null or empty for session {}", sessionId);
                userName = "Anonymous";
            }

            // Create the QuizResult object from the EndQuizResponse
            QuizResult quizResult = QuizResult.builder()
                    .userName(userName)
                    .score(endQuizResponse.getScore())
                    .percentageScore(endQuizResponse.getPercentageScore())
                    .totalQuestions(endQuizResponse.getTotalQuestions())
                    .correctAnswers(endQuizResponse.getCorrectAnswers())
                    // Handle potential type mismatch - EndQuizResponse might have duration as int or long
                    .timeTakenSeconds(convertToSeconds(endQuizResponse.getDuration()))
                    .completedAt(LocalDateTime.now())
                    .build();

            // Save the result
            quizRankingService.saveQuizResult(quizResult);

            logger.info("Recorded quiz result for user {} with score {}%",
                    userName, quizResult.getPercentageScore());

        } catch (Exception e) {
            logger.error("Error recording quiz result for session {}: {}", sessionId, e.getMessage(), e);
            // Don't throw exception here - we don't want to disrupt the normal flow if recording fails
        }
    }

    /**
     * Converts duration to seconds, handling various types of input.
     * This handles potential type mismatches between EndQuizResponse and QuizResult.
     */
    private int convertToSeconds(Object duration) {
        if (duration == null) {
            return 0;
        }

        if (duration instanceof Integer) {
            return (Integer) duration;
        } else if (duration instanceof Long) {
            return ((Long) duration).intValue();
        } else if (duration instanceof Double) {
            return ((Double) duration).intValue();
        } else if (duration instanceof String) {
            try {
                return Integer.parseInt((String) duration);
            } catch (NumberFormatException e) {
                logger.warn("Could not parse duration string: {}", duration);
                return 0;
            }
        } else {
            // Fallback, try to use toString and parse
            try {
                return Integer.parseInt(duration.toString());
            } catch (Exception e) {
                logger.warn("Could not convert duration to seconds: {}", duration);
                return 0;
            }
        }
    }
}