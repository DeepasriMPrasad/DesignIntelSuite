package com.quizmaster.controller;

import com.quizmaster.model.dto.*;
import com.quizmaster.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Quiz Master API is up and running!");
    }

    @PostMapping("/start")
    public ResponseEntity<StartQuizResponse> startQuiz(@Valid @RequestBody StartQuizRequest request) {
        return ResponseEntity.ok(quizService.startQuiz(request));
    }

    @GetMapping("/question")
    public ResponseEntity<QuestionResponse> getQuestion(@RequestParam String sessionId) {
        return ResponseEntity.ok(quizService.getQuestion(sessionId));
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateAnswerResponse> validateAnswer(@Valid @RequestBody ValidateAnswerRequest request) {
        return ResponseEntity.ok(quizService.validateAnswer(request));
    }

    @GetMapping("/score")
    public ResponseEntity<ScoreResponse> getScore(@RequestParam String sessionId) {
        return ResponseEntity.ok(quizService.getScore(sessionId));
    }

    @PostMapping("/end")
    public ResponseEntity<EndQuizResponse> endQuiz(@RequestParam String sessionId) {
        return ResponseEntity.ok(quizService.endQuiz(sessionId));
    }
}
