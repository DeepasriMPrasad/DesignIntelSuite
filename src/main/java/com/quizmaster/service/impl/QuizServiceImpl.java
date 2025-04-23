package com.quizmaster.service.impl;

import com.quizmaster.exception.QuizException;
import com.quizmaster.model.Option;
import com.quizmaster.model.Question;
import com.quizmaster.model.QuizSession;
import com.quizmaster.model.dto.*;
import com.quizmaster.service.QuizService;
import com.quizmaster.util.ExcelQuestionLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    
    private final ExcelQuestionLoader questionLoader;
    private final Map<String, QuizSession> quizSessions = new ConcurrentHashMap<>();
    
    private final int MAX_QUESTIONS_PER_SESSION = 5;
    private final int MAX_ATTEMPTS_PER_QUESTION = 3;
    
    @Override
    public StartQuizResponse startQuiz(StartQuizRequest request) {
        // Generate a unique session ID
        String sessionId = UUID.randomUUID().toString();
        
        // Create a new quiz session
        QuizSession session = QuizSession.builder()
                .sessionId(sessionId)
                .userName(request.getUserName())
                .questions(new ArrayList<>())
                .currentQuestionId(null)
                .currentAttempts(0)
                .completedQuestions(new HashSet<>())
                .correctAnswers(0)
                .active(true)
                .startTime(new Date())
                .build();
        
        // Store the session
        quizSessions.put(sessionId, session);
        
        return StartQuizResponse.builder()
                .sessionId(sessionId)
                .message("Quiz session started successfully")
                .userName(request.getUserName())
                .maxQuestions(MAX_QUESTIONS_PER_SESSION)
                .build();
    }
    
    @Override
    public QuestionResponse getQuestion(String sessionId) {
        // Get the session
        QuizSession session = getValidSession(sessionId);
        
        // Check if the session has already completed the maximum number of questions
        if (session.getCompletedQuestions().size() >= MAX_QUESTIONS_PER_SESSION) {
            throw new QuizException("You have completed all questions for this quiz session");
        }
        
        // If there's a current question and it's not completed, return it
        if (session.getCurrentQuestionId() != null && 
            !session.getCompletedQuestions().contains(session.getCurrentQuestionId())) {
            Question currentQuestion = getQuestionById(session.getCurrentQuestionId());
            return mapToQuestionResponse(currentQuestion, session);
        }
        
        // Get a random question that hasn't been completed yet
        List<Question> allQuestions = questionLoader.loadQuestions();
        List<Question> availableQuestions = allQuestions.stream()
                .filter(q -> !session.getCompletedQuestions().contains(q.getId()))
                .collect(Collectors.toList());
        
        if (availableQuestions.isEmpty()) {
            throw new QuizException("No more questions available");
        }
        
        Random random = new Random();
        Question randomQuestion = availableQuestions.get(random.nextInt(availableQuestions.size()));
        
        // Update the session
        session.setCurrentQuestionId(randomQuestion.getId());
        session.setCurrentAttempts(0);
        quizSessions.put(sessionId, session);
        
        return mapToQuestionResponse(randomQuestion, session);
    }
    
    @Override
    public ValidateAnswerResponse validateAnswer(ValidateAnswerRequest request) {
        QuizSession session = getValidSession(request.getSessionId());
        
        // Check if the question ID matches the current question
        if (!request.getQuestionId().equals(session.getCurrentQuestionId())) {
            throw new QuizException("This is not the current question for this session");
        }
        
        Question question = getQuestionById(request.getQuestionId());
        
        // Check if the question is already completed
        if (session.getCompletedQuestions().contains(question.getId())) {
            throw new QuizException("This question has already been completed");
        }
        
        // Increment attempts
        session.setCurrentAttempts(session.getCurrentAttempts() + 1);
        
        // Check if the answer is correct
        boolean isCorrect = question.getOptions().stream()
                .filter(Option::isCorrect)
                .anyMatch(option -> option.getId().equals(request.getAnswerId()));
        
        ValidateAnswerResponse.ValidateAnswerResponseBuilder responseBuilder = ValidateAnswerResponse.builder()
                .questionId(question.getId())
                .correct(isCorrect)
                .attempts(session.getCurrentAttempts())
                .maxAttempts(MAX_ATTEMPTS_PER_QUESTION);
        
        if (isCorrect) {
            // If correct, mark the question as completed and increment correct answers
            session.getCompletedQuestions().add(question.getId());
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
            session.setCurrentQuestionId(null);
            session.setCurrentAttempts(0);
            
            responseBuilder.message("Correct answer!");
            responseBuilder.remainingQuestions(MAX_QUESTIONS_PER_SESSION - session.getCompletedQuestions().size());
        } else {
            // If incorrect, check if max attempts reached
            if (session.getCurrentAttempts() >= MAX_ATTEMPTS_PER_QUESTION) {
                // Mark the question as completed but don't increment correct answers
                session.getCompletedQuestions().add(question.getId());
                session.setCurrentQuestionId(null);
                session.setCurrentAttempts(0);
                
                // Get the correct answer for feedback
                String correctAnswerId = question.getOptions().stream()
                        .filter(Option::isCorrect)
                        .map(Option::getId)
                        .findFirst()
                        .orElse("");
                
                responseBuilder.message("Incorrect answer. You've used all attempts. The correct answer was: " + 
                                        question.getOptions().stream()
                                                .filter(Option::isCorrect)
                                                .map(Option::getText)
                                                .findFirst()
                                                .orElse(""));
                responseBuilder.correctAnswerId(correctAnswerId);
                responseBuilder.remainingQuestions(MAX_QUESTIONS_PER_SESSION - session.getCompletedQuestions().size());
            } else {
                responseBuilder.message("Incorrect answer. Please try again. Attempts remaining: " + 
                                        (MAX_ATTEMPTS_PER_QUESTION - session.getCurrentAttempts()));
                responseBuilder.remainingAttempts(MAX_ATTEMPTS_PER_QUESTION - session.getCurrentAttempts());
            }
        }
        
        // Update the session
        quizSessions.put(request.getSessionId(), session);
        
        return responseBuilder.build();
    }
    
    @Override
    public ScoreResponse getScore(String sessionId) {
        QuizSession session = getValidSession(sessionId);
        
        return ScoreResponse.builder()
                .sessionId(sessionId)
                .userName(session.getUserName())
                .totalQuestions(session.getCompletedQuestions().size())
                .correctAnswers(session.getCorrectAnswers())
                .quizComplete(session.getCompletedQuestions().size() >= MAX_QUESTIONS_PER_SESSION)
                .percentageScore(calculatePercentageScore(session))
                .build();
    }
    
    @Override
    public EndQuizResponse endQuiz(String sessionId) {
        QuizSession session = getValidSession(sessionId);
        
        // Mark the session as inactive
        session.setActive(false);
        session.setEndTime(new Date());
        quizSessions.put(sessionId, session);
        
        return EndQuizResponse.builder()
                .sessionId(sessionId)
                .userName(session.getUserName())
                .totalQuestions(session.getCompletedQuestions().size())
                .correctAnswers(session.getCorrectAnswers())
                .percentageScore(calculatePercentageScore(session))
                .duration(calculateDuration(session))
                .message("Quiz completed successfully")
                .build();
    }
    
    private QuizSession getValidSession(String sessionId) {
        QuizSession session = quizSessions.get(sessionId);
        if (session == null) {
            throw new QuizException("Invalid or expired session ID");
        }
        if (!session.isActive()) {
            throw new QuizException("This quiz session has already ended");
        }
        return session;
    }
    
    private Question getQuestionById(String questionId) {
        return questionLoader.loadQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new QuizException("Question not found"));
    }
    
    private QuestionResponse mapToQuestionResponse(Question question, QuizSession session) {
        return QuestionResponse.builder()
                .questionId(question.getId())
                .text(question.getText())
                .options(question.getOptions().stream()
                        .map(option -> new QuestionResponse.OptionDto(option.getId(), option.getText()))
                        .collect(Collectors.toList()))
                .attempts(session.getCurrentAttempts())
                .maxAttempts(MAX_ATTEMPTS_PER_QUESTION)
                .completedQuestions(session.getCompletedQuestions().size())
                .totalQuestions(MAX_QUESTIONS_PER_SESSION)
                .build();
    }
    
    private double calculatePercentageScore(QuizSession session) {
        if (session.getCompletedQuestions().isEmpty()) {
            return 0.0;
        }
        return (double) session.getCorrectAnswers() / session.getCompletedQuestions().size() * 100;
    }
    
    private long calculateDuration(QuizSession session) {
        Date endTime = session.getEndTime() != null ? session.getEndTime() : new Date();
        return (endTime.getTime() - session.getStartTime().getTime()) / 1000; // Duration in seconds
    }
}
