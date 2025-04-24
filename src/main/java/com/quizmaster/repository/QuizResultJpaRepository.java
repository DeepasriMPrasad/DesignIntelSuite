package com.quizmaster.repository;

import com.quizmaster.model.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResultJpaRepository extends JpaRepository<QuizResult, Long> {
    
    List<QuizResult> findByUserNameOrderByPercentageScoreDesc(String userName);
    
    @Query("SELECT q FROM QuizResult q ORDER BY q.percentageScore DESC, q.timeTakenSeconds ASC")
    List<QuizResult> findTopScores();
    
    @Query("SELECT q FROM QuizResult q WHERE q.sessionId = ?1")
    QuizResult findBySessionId(String sessionId);
}