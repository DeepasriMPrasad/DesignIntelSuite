package com.quizmaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizmaster.model.dto.*;
import com.quizmaster.service.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuizController.class)
public class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizService quizService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testStartQuiz() throws Exception {
        // Arrange
        StartQuizRequest request = StartQuizRequest.builder()
                .userName("TestUser")
                .build();
        
        StartQuizResponse response = StartQuizResponse.builder()
                .sessionId("test-session-id")
                .userName("TestUser")
                .message("Quiz session started successfully")
                .maxQuestions(5)
                .build();
        
        when(quizService.startQuiz(any(StartQuizRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/quiz/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").value("test-session-id"))
                .andExpect(jsonPath("$.userName").value("TestUser"))
                .andExpect(jsonPath("$.message").value("Quiz session started successfully"))
                .andExpect(jsonPath("$.maxQuestions").value(5));
        
        verify(quizService, times(1)).startQuiz(any(StartQuizRequest.class));
    }

    @Test
    void testGetQuestion() throws Exception {
        // Arrange
        String sessionId = "test-session-id";
        
        QuestionResponse.OptionDto option1 = new QuestionResponse.OptionDto("opt1", "Option 1");
        QuestionResponse.OptionDto option2 = new QuestionResponse.OptionDto("opt2", "Option 2");
        QuestionResponse.OptionDto option3 = new QuestionResponse.OptionDto("opt3", "Option 3");
        QuestionResponse.OptionDto option4 = new QuestionResponse.OptionDto("opt4", "Option 4");
        
        QuestionResponse response = QuestionResponse.builder()
                .questionId("q1")
                .text("Sample question?")
                .options(java.util.List.of(option1, option2, option3, option4))
                .attempts(0)
                .maxAttempts(3)
                .completedQuestions(0)
                .totalQuestions(5)
                .build();
        
        when(quizService.getQuestion(sessionId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/quiz/question")
                .param("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.questionId").value("q1"))
                .andExpect(jsonPath("$.text").value("Sample question?"))
                .andExpect(jsonPath("$.options.length()").value(4))
                .andExpect(jsonPath("$.attempts").value(0))
                .andExpect(jsonPath("$.maxAttempts").value(3));
        
        verify(quizService, times(1)).getQuestion(sessionId);
    }

    @Test
    void testValidateAnswer() throws Exception {
        // Arrange
        ValidateAnswerRequest request = ValidateAnswerRequest.builder()
                .sessionId("test-session-id")
                .questionId("q1")
                .answerId("opt2")
                .build();
        
        ValidateAnswerResponse response = ValidateAnswerResponse.builder()
                .questionId("q1")
                .correct(true)
                .message("Correct answer!")
                .attempts(1)
                .maxAttempts(3)
                .remainingQuestions(4)
                .build();
        
        when(quizService.validateAnswer(any(ValidateAnswerRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/quiz/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.questionId").value("q1"))
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.message").value("Correct answer!"))
                .andExpect(jsonPath("$.attempts").value(1))
                .andExpect(jsonPath("$.maxAttempts").value(3))
                .andExpect(jsonPath("$.remainingQuestions").value(4));
        
        verify(quizService, times(1)).validateAnswer(any(ValidateAnswerRequest.class));
    }

    @Test
    void testGetScore() throws Exception {
        // Arrange
        String sessionId = "test-session-id";
        
        ScoreResponse response = ScoreResponse.builder()
                .sessionId(sessionId)
                .userName("TestUser")
                .totalQuestions(3)
                .correctAnswers(2)
                .percentageScore(66.67)
                .quizComplete(false)
                .build();
        
        when(quizService.getScore(sessionId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/quiz/score")
                .param("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.userName").value("TestUser"))
                .andExpect(jsonPath("$.totalQuestions").value(3))
                .andExpect(jsonPath("$.correctAnswers").value(2))
                .andExpect(jsonPath("$.percentageScore").value(66.67))
                .andExpect(jsonPath("$.quizComplete").value(false));
        
        verify(quizService, times(1)).getScore(sessionId);
    }

    @Test
    void testEndQuiz() throws Exception {
        // Arrange
        String sessionId = "test-session-id";
        
        EndQuizResponse response = EndQuizResponse.builder()
                .sessionId(sessionId)
                .userName("TestUser")
                .totalQuestions(5)
                .correctAnswers(4)
                .percentageScore(80.0)
                .duration(120)
                .message("Quiz completed successfully")
                .build();
        
        when(quizService.endQuiz(sessionId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/quiz/end")
                .param("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.userName").value("TestUser"))
                .andExpect(jsonPath("$.totalQuestions").value(5))
                .andExpect(jsonPath("$.correctAnswers").value(4))
                .andExpect(jsonPath("$.percentageScore").value(80.0))
                .andExpect(jsonPath("$.duration").value(120))
                .andExpect(jsonPath("$.message").value("Quiz completed successfully"));
        
        verify(quizService, times(1)).endQuiz(sessionId);
    }
}
