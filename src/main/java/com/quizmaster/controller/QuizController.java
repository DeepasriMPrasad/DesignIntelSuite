package com.quizmaster.controller;

import com.quizmaster.model.dto.*;
import com.quizmaster.service.QuizResultRecorder;
import com.quizmaster.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@Tag(name = "Quiz API", description = "Operations for managing quiz sessions")
public class QuizController {

    private final QuizService quizService;
    private final QuizResultRecorder quizResultRecorder;

    @Autowired
    public QuizController(QuizService quizService, QuizResultRecorder quizResultRecorder) {
        this.quizService = quizService;
        this.quizResultRecorder = quizResultRecorder;
    }

    @Operation(summary = "Health check endpoint",
            description = "Simple endpoint to verify the API is running")
    @ApiResponse(responseCode = "200", description = "API is working",
            content = @Content(schema = @Schema(implementation = String.class)))
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Quiz Master API is up and running!");
    }

    @Operation(summary = "Start a new quiz session",
            description = "Creates a new quiz session for the given user")
    @ApiResponse(responseCode = "200", description = "Quiz session created successfully",
            content = @Content(schema = @Schema(implementation = StartQuizResponse.class)))
    @PostMapping("/start")
    public ResponseEntity<StartQuizResponse> startQuiz(@Valid @RequestBody StartQuizRequest request) {
        return ResponseEntity.ok(quizService.startQuiz(request));
    }

    @Operation(summary = "Get current question",
            description = "Retrieves the current question for the active session")
    @ApiResponse(responseCode = "200", description = "Question retrieved successfully",
            content = @Content(schema = @Schema(implementation = QuestionResponse.class)))
    @GetMapping("/question")
    public ResponseEntity<QuestionResponse> getQuestion(@RequestParam String sessionId) {
        return ResponseEntity.ok(quizService.getQuestion(sessionId));
    }

    @Operation(summary = "Validate answer",
            description = "Validates the user's answer to the current question")
    @ApiResponse(responseCode = "200", description = "Answer validated",
            content = @Content(schema = @Schema(implementation = ValidateAnswerResponse.class)))
    @PostMapping("/validate")
    public ResponseEntity<ValidateAnswerResponse> validateAnswer(@Valid @RequestBody ValidateAnswerRequest request) {
        return ResponseEntity.ok(quizService.validateAnswer(request));
    }

    @Operation(summary = "Get current score",
            description = "Retrieves the current score for the active session")
    @ApiResponse(responseCode = "200", description = "Score retrieved successfully",
            content = @Content(schema = @Schema(implementation = ScoreResponse.class)))
    @GetMapping("/score")
    public ResponseEntity<ScoreResponse> getScore(@RequestParam String sessionId) {
        return ResponseEntity.ok(quizService.getScore(sessionId));
    }

    @Operation(summary = "End quiz session",
            description = "Ends the quiz session and returns final results")
    @ApiResponse(responseCode = "200", description = "Quiz ended successfully",
            content = @Content(schema = @Schema(implementation = EndQuizResponse.class)))
    @PostMapping("/end")
    public ResponseEntity<EndQuizResponse> endQuiz(@RequestParam String sessionId) {

        String userName = quizService.getSessionUserName(sessionId);
        EndQuizResponse response = quizService.endQuiz(sessionId);
        quizResultRecorder.recordQuizResult(sessionId, userName, response);
        return ResponseEntity.ok(response);
    }


}