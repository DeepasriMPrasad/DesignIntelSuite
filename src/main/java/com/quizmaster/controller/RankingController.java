package com.quizmaster.controller;

import com.quizmaster.model.QuizResult;
import com.quizmaster.model.dto.RankingListResponse;
import com.quizmaster.model.dto.RankingResponse;
import com.quizmaster.service.QuizRankingService;
import com.quizmaster.service.ScheduledExportService;
import com.quizmaster.util.ExcelReportGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@Tag(name = "Rankings API", description = "Operations for retrieving quiz rankings")
public class RankingController {

    private final QuizRankingService quizRankingService;
    private final ExcelReportGenerator excelReportGenerator;
    private final ScheduledExportService scheduledExportService;

    @Autowired
    public RankingController(@Qualifier("jpaQuizRankingService") QuizRankingService quizRankingService, 
                            ExcelReportGenerator excelReportGenerator,
                            ScheduledExportService scheduledExportService) {
        this.quizRankingService = quizRankingService;
        this.excelReportGenerator = excelReportGenerator;
        this.scheduledExportService = scheduledExportService;
    }

    @Operation(summary = "Get leaderboard",
            description = "Retrieves the top quiz participants by score")
    @ApiResponse(responseCode = "200", description = "Leaderboard retrieved successfully",
            content = @Content(schema = @Schema(implementation = RankingListResponse.class)))
    @GetMapping("/leaderboard")
    public ResponseEntity<RankingListResponse> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {

        List<QuizResult> topResults = quizRankingService.getTopResults(limit);

        RankingListResponse response = RankingListResponse.builder()
                .rankings(topResults)
                .totalParticipants(quizRankingService.getAllResults().size())
                .generatedAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user ranking",
            description = "Retrieves the ranking information for a specific user")
    @ApiResponse(responseCode = "200", description = "User ranking retrieved successfully",
            content = @Content(schema = @Schema(implementation = RankingResponse.class)))
    @GetMapping("/user/{userName}")
    public ResponseEntity<RankingResponse> getUserRanking(@PathVariable String userName) {
        RankingResponse ranking = quizRankingService.getUserRanking(userName);
        return ResponseEntity.ok(ranking);
    }

    @Operation(summary = "Download rankings report",
            description = "Downloads a report of all quiz rankings in Excel format")
    @GetMapping("/download/excel")
    public void downloadExcelReport(HttpServletResponse response) {
        try {
            List<QuizResult> results = quizRankingService.getAllResults();

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=quiz_rankings.xlsx");

            excelReportGenerator.generateRankingsReport(results, response.getOutputStream());

        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }

    @Operation(summary = "Clear all rankings",
            description = "Deletes all stored quiz results (admin function)")
    @ApiResponse(responseCode = "204", description = "Rankings cleared successfully")
    @DeleteMapping("/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearAllRankings() throws IOException {
        quizRankingService.clearAllResults();
    }
    
    @Operation(summary = "Manually export quiz results to Excel",
            description = "Triggers manual export of quiz results to Excel file")
    @ApiResponse(responseCode = "200", description = "Export process triggered")
    @PostMapping("/export-now")
    public ResponseEntity<String> manualExport() {
        int results = scheduledExportService.exportResultsNow();
        if (results < 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error occurred during export process");
        } else if (results == 0) {
            return ResponseEntity.ok("No quiz results to export");
        } else {
            return ResponseEntity.ok("Successfully exported " + results + " quiz results to Excel");
        }
    }
}