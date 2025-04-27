package com.quizmaster.controller;

import com.quizmaster.model.QuizResult;
import com.quizmaster.model.dto.*;
import com.quizmaster.service.QuizRankingService;
import com.quizmaster.service.QuizResultRecorder;
import com.quizmaster.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@Tag(name = "Quiz API", description = "Operations for managing quiz sessions")
@Slf4j
public class QuizController {

    private final QuizService quizService;
    private final QuizResultRecorder quizResultRecorder;
    private final QuizRankingService quizRankingService;

    @Autowired
    public QuizController(
            QuizService quizService, 
            @Qualifier("jpaQuizResultRecorder") QuizResultRecorder quizResultRecorder,
            @Qualifier("jpaQuizRankingService") QuizRankingService quizRankingService) {
        this.quizService = quizService;
        this.quizResultRecorder = quizResultRecorder;
        this.quizRankingService = quizRankingService;
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
    
    @Operation(summary = "Clear leaderboard data (Admin only)",
            description = "Clears all quiz results from the leaderboard")
    @ApiResponse(responseCode = "200", description = "Leaderboard data cleared successfully")
    @PostMapping("/admin/clear-leaderboard")
    public ResponseEntity<String> clearLeaderboard() {
        try {
            // Export to db_results.xlsx before clearing the DB
            exportToDbResultsFile();
            
            quizRankingService.clearAllResults();
            return ResponseEntity.ok("Leaderboard data exported to db_results.xlsx and cleared successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error clearing leaderboard data: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to export the current DB data to a special db_results.xlsx file
     * before deletion
     */
    private void exportToDbResultsFile() {
        try {
            // Get all results from the database
            var results = quizRankingService.getAllResults();
            
            // Export them to db_results.xlsx file
            // We'll create a simple wrapper around the existing export functionality
            if (quizResultRecorder instanceof com.quizmaster.service.impl.JpaQuizResultRecorder) {
                ((com.quizmaster.service.impl.JpaQuizResultRecorder) quizResultRecorder)
                    .exportResultsToSpecialFile(results, "db_results.xlsx");
            }
        } catch (Exception e) {
            // Log the error but don't block the request
            org.slf4j.LoggerFactory.getLogger(QuizController.class)
                .error("Failed to export DB results to db_results.xlsx before clearing: {}", e.getMessage(), e);
        }
    }
    
    @Operation(summary = "Delete individual quiz result (Admin only)",
            description = "Deletes a specific quiz result from the leaderboard")
    @ApiResponse(responseCode = "200", description = "Quiz result deleted successfully")
    @PostMapping("/admin/delete-result")
    public ResponseEntity<String> deleteQuizResult(@RequestParam Long id) {
        try {
            quizRankingService.deleteQuizResult(id);
            return ResponseEntity.ok("Quiz result deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting quiz result: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Export database results to db_results.xlsx (Admin only)",
            description = "Exports all quiz results to db_results.xlsx file without deleting them")
    @ApiResponse(responseCode = "200", description = "Quiz results exported successfully")
    @PostMapping("/admin/export-to-db-results")
    public ResponseEntity<String> exportToDbResults() {
        try {
            exportToDbResultsFile();
            return ResponseEntity.ok("Database results successfully exported to db_results.xlsx");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error exporting database results: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Export database to file (Admin only)",
            description = "Exports all quiz results to db_results.xlsx file - alias endpoint")
    @ApiResponse(responseCode = "200", description = "Quiz results exported successfully")
    @GetMapping("/admin/export-to-db-file")
    public ResponseEntity<String> exportToDbFile() {
        return exportToDbResults();
    }
    
    @Operation(summary = "Add new quiz result entry (Admin only)",
            description = "Adds a new quiz result entry to the database")
    @ApiResponse(responseCode = "200", description = "Entry added successfully")
    @ApiResponse(responseCode = "400", description = "Invalid entry data")
    @PostMapping("/admin/add-entry")
    public ResponseEntity<String> addEntry(@Valid @RequestBody AddEntryRequest request) {
        try {
            log.info("Adding new quiz result entry for user: {}", request.getUserName());
            
            // Create a new QuizResult entity
            QuizResult quizResult = QuizResult.builder()
                    .userName(request.getUserName())
                    .iNumber(request.getINumber())
                    .score(request.getCorrectAnswers())
                    .correctAnswers(request.getCorrectAnswers())
                    .totalQuestions(request.getTotalQuestions())
                    .percentageScore(request.getPercentageScore())
                    .timeTakenSeconds(request.getTimeTakenSeconds())
                    .completedAt(request.getCompletedAt())
                    .build();
            
            // Save to database
            quizRankingService.saveQuizResult(quizResult);
            
            // Export to Excel to ensure consistency
            try {
                List<QuizResult> allResults = quizRankingService.getAllResults();
                if (quizResultRecorder instanceof com.quizmaster.service.impl.JpaQuizResultRecorder) {
                    ((com.quizmaster.service.impl.JpaQuizResultRecorder) quizResultRecorder)
                        .exportResults(allResults);
                }
            } catch (Exception e) {
                log.error("Failed to export results after adding new entry: {}", e.getMessage(), e);
                // Continue anyway since the DB save was successful
            }
            
            return ResponseEntity.ok("Entry added successfully");
        } catch (Exception e) {
            log.error("Error adding new entry: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error adding new entry: " + e.getMessage());
        }
    }

}