package com.quizmaster.service.impl;

import com.quizmaster.model.QuizResult;
import com.quizmaster.model.dto.EndQuizResponse;
import com.quizmaster.repository.QuizResultJpaRepository;
import com.quizmaster.service.QuizResultExporter;
import com.quizmaster.service.QuizResultRecorder;
import com.quizmaster.service.impl.ExcelQuizResultExporterImpl.ExportTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * JPA implementation of QuizResultRecorder that persists quiz results to a database
 */
@Service
@Slf4j
public class JpaQuizResultRecorder implements QuizResultRecorder {

    private final QuizResultJpaRepository quizResultRepository;
    private final QuizResultExporter quizResultExporter;

    @Autowired
    public JpaQuizResultRecorder(QuizResultJpaRepository quizResultRepository, 
                                QuizResultExporter quizResultExporter) {
        this.quizResultRepository = quizResultRepository;
        this.quizResultExporter = quizResultExporter;
    }

    @Override
    public void recordQuizResult(String sessionId, String userName, EndQuizResponse response) {
        log.info("Recording quiz result for session: {} and user: {}", sessionId, userName);
        
        QuizResult quizResult = QuizResult.builder()
                .sessionId(sessionId)
                .userName(userName)
                .iNumber(response.getINumber())
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
    
    /**
     * Exports quiz results to a special file (e.g., db_results.xlsx)
     * This is used before clearing the database to ensure no data is lost
     * 
     * @param results List of quiz results to export
     * @param filename The filename for the export (e.g., "db_results.xlsx")
     */
    public void exportResultsToSpecialFile(List<QuizResult> results, String filename) {
        if (results == null || results.isEmpty()) {
            log.info("No results to export to {}", filename);
            return;
        }
        
        log.info("Exporting {} quiz results to special file: {}", results.size(), filename);
        
        // Check if the regular exporter can handle this
        if (quizResultExporter instanceof ExcelQuizResultExporterImpl) {
            try {
                // Create a custom export based on the filename
                createCustomExcelExport(results, filename);
                log.info("Successfully exported {} quiz results to {}", results.size(), filename);
            } catch (Exception e) {
                log.error("Error creating custom export to {}: {}", filename, e.getMessage(), e);
            }
        } else {
            // Fallback to regular export functionality
            log.info("Using standard export for {}", filename);
            quizResultExporter.exportResults(results, ExportTrigger.MANUAL);
        }
    }
    
    /**
     * Creates a custom Excel export to a specified file
     */
    private void createCustomExcelExport(List<QuizResult> results, String filename) throws IOException {
        final String[] HEADERS = {
            "ID", "User Name", "I-Number", "Score (%)", "Total Questions", 
            "Correct Answers", "Time (sec)", "Completed At"
        };
        final String SHEET_NAME = "Quiz Results";
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(SHEET_NAME);
        
        // Create header row with styles
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (QuizResult result : results) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(result.getId() != null ? result.getId() : rowNum - 1);
            row.createCell(1).setCellValue(result.getUserName());
            row.createCell(2).setCellValue(result.getINumber());
            row.createCell(3).setCellValue(result.getPercentageScore());
            row.createCell(4).setCellValue(result.getTotalQuestions());
            row.createCell(5).setCellValue(result.getCorrectAnswers());
            row.createCell(6).setCellValue(result.getTimeTakenSeconds());
            row.createCell(7).setCellValue(result.getCompletedAt() != null ? 
                    result.getCompletedAt().format(formatter) : 
                    LocalDateTime.now().format(formatter));
        }
        
        // Auto-size columns
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(filename)) {
            workbook.write(fileOut);
        }
        
        workbook.close();
    }
}