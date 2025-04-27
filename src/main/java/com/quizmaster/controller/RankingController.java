package com.quizmaster.controller;

import com.quizmaster.model.QuizResult;
import com.quizmaster.model.dto.RankingListResponse;
import com.quizmaster.model.dto.RankingResponse;
import com.quizmaster.service.QuizRankingService;
import com.quizmaster.service.ScheduledExportService;
import com.quizmaster.util.ExcelReportGenerator;
import com.quizmaster.util.LoggingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
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

    private static final Logger log = LoggingUtils.getLogger(RankingController.class);
    
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

        LoggingUtils.logApiRequest(log, "GET", "/api/rankings/leaderboard", "limit=" + limit);
        
        List<QuizResult> topResults = quizRankingService.getTopResults(limit);
        int totalParticipants = quizRankingService.getAllResults().size();
        
        LoggingUtils.logDatabaseOperation(log, "SELECT", "QuizResults", 
                String.format("Retrieved top %d results out of %d total", topResults.size(), totalParticipants));

        RankingListResponse response = RankingListResponse.builder()
                .rankings(topResults)
                .totalParticipants(totalParticipants)
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
        LoggingUtils.logApiRequest(log, "GET", "/api/rankings/user/" + userName);
        RankingResponse ranking = quizRankingService.getUserRanking(userName);
        LoggingUtils.logDatabaseOperation(log, "SELECT", "QuizResults", "Retrieved ranking for user: " + userName);
        return ResponseEntity.ok(ranking);
    }

    @Operation(summary = "Download rankings report (Admin only)",
            description = "Downloads a report of all quiz rankings in Excel format. Restricted to admin access.")
    @GetMapping("/admin/download/excel")
    public void downloadExcelReport(HttpServletResponse response) {
        try {
            LoggingUtils.logApiRequest(log, "GET", "/api/rankings/admin/download/excel");
            LoggingUtils.logEvent(log, "AdminOperation", "Attempting Excel download");
            
            List<QuizResult> results = quizRankingService.getAllResults();
            LoggingUtils.logEvent(log, "ExcelDownload", "Generating Excel download with " + results.size() + " records");

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=quiz_rankings.xlsx");

            excelReportGenerator.generateRankingsReport(results, response.getOutputStream());
            LoggingUtils.logEvent(log, "ExcelDownloadComplete", "Excel report generated and sent to client");

        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to generate Excel report", e);
            throw new RuntimeException("Error generating Excel report", e);
        }
    }
    
    // Keep the old endpoint for backward compatibility but mark it as deprecated
    @Operation(summary = "Download rankings report (Deprecated)",
            description = "This endpoint is deprecated. Use /admin/download/excel instead.")
    @GetMapping("/download/excel")
    public void downloadExcelReportDeprecated(HttpServletResponse response) {
        LoggingUtils.logApiRequest(log, "GET", "/api/rankings/download/excel");
        LoggingUtils.logEvent(log, "DeprecatedEndpoint", "Using deprecated Excel download endpoint");
        downloadExcelReport(response);
    }

    @Operation(summary = "Clear all rankings",
            description = "Deletes all stored quiz results (admin function)")
    @ApiResponse(responseCode = "204", description = "Rankings cleared successfully")
    @DeleteMapping("/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearAllRankings() throws IOException {
        LoggingUtils.logApiRequest(log, "DELETE", "/api/rankings/clear");
        LoggingUtils.logEvent(log, "AdminOperation", "Clearing all leaderboard rankings");
        quizRankingService.clearAllResults();
        LoggingUtils.logDatabaseOperation(log, "DELETE", "QuizResults", "All quiz results cleared from system");
    }
    
    @Operation(summary = "Manually export quiz results to Excel",
            description = "Triggers manual export of quiz results to Excel file")
    @ApiResponse(responseCode = "200", description = "Export process triggered")
    @PostMapping("/export-now")
    public ResponseEntity<String> manualExport() {
        LoggingUtils.logApiRequest(log, "POST", "/api/rankings/export-now");
        LoggingUtils.logEvent(log, "ManualExport", "Manual export of quiz results to Excel initiated");
        
        int results = scheduledExportService.exportResultsNow();
        if (results < 0) {
            LoggingUtils.logError(log, "Manual export failed", new RuntimeException("Export error"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error occurred during export process");
        } else if (results == 0) {
            LoggingUtils.logEvent(log, "EmptyExport", "No quiz results to export");
            return ResponseEntity.ok("No quiz results to export");
        } else {
            LoggingUtils.logEvent(log, "ExportSuccess", "Successfully exported " + results + " quiz results to Excel");
            return ResponseEntity.ok("Successfully exported " + results + " quiz results to Excel");
        }
    }
}