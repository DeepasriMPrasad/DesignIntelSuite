package com.quizmaster.service.impl;

import com.quizmaster.model.QuizResult;
import com.quizmaster.model.dto.EndQuizResponse;
import com.quizmaster.repository.QuizResultJpaRepository;
import com.quizmaster.service.QuizResultRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * JPA implementation of QuizResultRecorder that persists quiz results to a database
 */
@Service
@Slf4j
public class JpaQuizResultRecorder implements QuizResultRecorder {

    private final QuizResultJpaRepository quizResultRepository;

    @Autowired
    public JpaQuizResultRecorder(QuizResultJpaRepository quizResultRepository) {
        this.quizResultRepository = quizResultRepository;
    }

    @Override
    public void recordQuizResult(String sessionId, String userName, EndQuizResponse response) {
        log.info("Recording quiz result for session: {} and user: {}", sessionId, userName);
        
        QuizResult quizResult = QuizResult.builder()
                .sessionId(sessionId)
                .userName(userName)
                .score(response.getCorrectAnswers())
                .percentageScore(response.getPercentageScore())
                .totalQuestions(response.getTotalQuestions())
                .correctAnswers(response.getCorrectAnswers())
                .timeTakenSeconds((int) response.getDuration())
                .completedAt(LocalDateTime.now())
                .build();
                
        quizResultRepository.save(quizResult);
        log.info("Quiz result successfully saved to database");
    }
}