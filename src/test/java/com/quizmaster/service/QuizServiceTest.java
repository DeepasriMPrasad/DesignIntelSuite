package com.quizmaster.service;

import com.quizmaster.exception.QuizException;
import com.quizmaster.model.Option;
import com.quizmaster.model.Question;
import com.quizmaster.model.dto.*;
import com.quizmaster.service.impl.QuizServiceImpl;
import com.quizmaster.util.ExcelQuestionLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizServiceTest {

    @Mock
    private ExcelQuestionLoader questionLoader;

    @InjectMocks
    private QuizServiceImpl quizService;

    private Question sampleQuestion;
    private List<Question> mockQuestions;

    @BeforeEach
    void setUp() {
        // Create a sample question
        Option option1 = Option.builder().id("opt1").text("Option 1").correct(false).build();
        Option option2 = Option.builder().id("opt2").text("Option 2").correct(true).build();
        Option option3 = Option.builder().id("opt3").text("Option 3").correct(false).build();
        Option option4 = Option.builder().id("opt4").text("Option 4").correct(false).build();

        sampleQuestion = Question.builder()
                .id("q1")
                .text("Sample question?")
                .options(Arrays.asList(option1, option2, option3, option4))
                .build();

        mockQuestions = List.of(sampleQuestion);

        // Mock the question loader
        when(questionLoader.loadQuestions()).thenReturn(mockQuestions);
    }

    @Test
    void testStartQuiz() {
        // Arrange
        StartQuizRequest request = StartQuizRequest.builder()
                .userName("TestUser")
                .build();

        // Act
        StartQuizResponse response = quizService.startQuiz(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getSessionId());
        assertEquals("TestUser", response.getUserName());
        assertEquals("Quiz session started successfully", response.getMessage());
        assertEquals(5, response.getMaxQuestions());
    }

    @Test
    void testGetQuestion() {
        // Arrange
        StartQuizRequest request = StartQuizRequest.builder()
                .userName("TestUser")
                .build();
        StartQuizResponse startResponse = quizService.startQuiz(request);

        // Act
        QuestionResponse questionResponse = quizService.getQuestion(startResponse.getSessionId());

        // Assert
        assertNotNull(questionResponse);
        assertEquals("q1", questionResponse.getQuestionId());
        assertEquals("Sample question?", questionResponse.getText());
        assertEquals(4, questionResponse.getOptions().size());
        assertEquals(0, questionResponse.getAttempts());
        assertEquals(3, questionResponse.getMaxAttempts());
    }

    @Test
    void testValidateCorrectAnswer() {
        // Arrange
        StartQuizRequest request = StartQuizRequest.builder()
                .userName("TestUser")
                .build();
        StartQuizResponse startResponse = quizService.startQuiz(request);
        QuestionResponse questionResponse = quizService.getQuestion(startResponse.getSessionId());

        ValidateAnswerRequest validateRequest = ValidateAnswerRequest.builder()
                .sessionId(startResponse.getSessionId())
                .questionId(questionResponse.getQuestionId())
                .answerId("opt2") // Correct answer
                .build();

        // Act
        ValidateAnswerResponse validateResponse = quizService.validateAnswer(validateRequest);

        // Assert
        assertNotNull(validateResponse);
        assertTrue(validateResponse.isCorrect());
        assertEquals("Correct answer!", validateResponse.getMessage());
        assertEquals(1, validateResponse.getAttempts());
        assertEquals(3, validateResponse.getMaxAttempts());
        assertEquals(4, validateResponse.getRemainingQuestions());
    }

    @Test
    void testValidateIncorrectAnswer() {
        // Arrange
        StartQuizRequest request = StartQuizRequest.builder()
                .userName("TestUser")
                .build();
        StartQuizResponse startResponse = quizService.startQuiz(request);
        QuestionResponse questionResponse = quizService.getQuestion(startResponse.getSessionId());

        ValidateAnswerRequest validateRequest = ValidateAnswerRequest.builder()
                .sessionId(startResponse.getSessionId())
                .questionId(questionResponse.getQuestionId())
                .answerId("opt1") // Incorrect answer
                .build();

        // Act
        ValidateAnswerResponse validateResponse = quizService.validateAnswer(validateRequest);

        // Assert
        assertNotNull(validateResponse);
        assertFalse(validateResponse.isCorrect());
        assertTrue(validateResponse.getMessage().contains("Incorrect answer"));
        assertEquals(1, validateResponse.getAttempts());
        assertEquals(3, validateResponse.getMaxAttempts());
        assertEquals(2, validateResponse.getRemainingAttempts());
    }

    @Test
    void testGetScore() {
        // Arrange
        StartQuizRequest request = StartQuizRequest.builder()
                .userName("TestUser")
                .build();
        StartQuizResponse startResponse = quizService.startQuiz(request);
        
        // Get a question and answer it correctly
        QuestionResponse questionResponse = quizService.getQuestion(startResponse.getSessionId());
        ValidateAnswerRequest validateRequest = ValidateAnswerRequest.builder()
                .sessionId(startResponse.getSessionId())
                .questionId(questionResponse.getQuestionId())
                .answerId("opt2") // Correct answer
                .build();
        quizService.validateAnswer(validateRequest);

        // Act
        ScoreResponse scoreResponse = quizService.getScore(startResponse.getSessionId());

        // Assert
        assertNotNull(scoreResponse);
        assertEquals(startResponse.getSessionId(), scoreResponse.getSessionId());
        assertEquals("TestUser", scoreResponse.getUserName());
        assertEquals(1, scoreResponse.getTotalQuestions());
        assertEquals(1, scoreResponse.getCorrectAnswers());
        assertEquals(100.0, scoreResponse.getPercentageScore());
        assertFalse(scoreResponse.isQuizComplete());
    }

    @Test
    void testEndQuiz() {
        // Arrange
        StartQuizRequest request = StartQuizRequest.builder()
                .userName("TestUser")
                .build();
        StartQuizResponse startResponse = quizService.startQuiz(request);
        
        // Get a question and answer it correctly
        QuestionResponse questionResponse = quizService.getQuestion(startResponse.getSessionId());
        ValidateAnswerRequest validateRequest = ValidateAnswerRequest.builder()
                .sessionId(startResponse.getSessionId())
                .questionId(questionResponse.getQuestionId())
                .answerId("opt2") // Correct answer
                .build();
        quizService.validateAnswer(validateRequest);

        // Act
        EndQuizResponse endResponse = quizService.endQuiz(startResponse.getSessionId());

        // Assert
        assertNotNull(endResponse);
        assertEquals(startResponse.getSessionId(), endResponse.getSessionId());
        assertEquals("TestUser", endResponse.getUserName());
        assertEquals(1, endResponse.getTotalQuestions());
        assertEquals(1, endResponse.getCorrectAnswers());
        assertEquals(100.0, endResponse.getPercentageScore());
        assertTrue(endResponse.getDuration() >= 0);
        assertEquals("Quiz completed successfully", endResponse.getMessage());
        
        // Verify that trying to get a question after ending throws an exception
        assertThrows(QuizException.class, () -> quizService.getQuestion(startResponse.getSessionId()));
    }
}
