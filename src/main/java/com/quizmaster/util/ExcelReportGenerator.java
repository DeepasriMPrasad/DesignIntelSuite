package com.quizmaster.util;

import com.quizmaster.model.QuizResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ExcelReportGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void generateRankingsReport(List<QuizResult> results, OutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Quiz Rankings");

            // Create styles
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCell(headerRow, 0, "Rank", headerStyle);
            createHeaderCell(headerRow, 1, "User Name", headerStyle);
            createHeaderCell(headerRow, 2, "Score", headerStyle);
            createHeaderCell(headerRow, 3, "Percentage", headerStyle);
            createHeaderCell(headerRow, 4, "Questions", headerStyle);
            createHeaderCell(headerRow, 5, "Correct", headerStyle);
            createHeaderCell(headerRow, 6, "Time (sec)", headerStyle);
            createHeaderCell(headerRow, 7, "Completed At", headerStyle);

            // Create data rows
            for (int i = 0; i < results.size(); i++) {
                QuizResult result = results.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(result.getRank());
                row.createCell(1).setCellValue(result.getUserName());
                row.createCell(2).setCellValue(result.getScore());
                row.createCell(3).setCellValue(String.format("%.2f%%", result.getPercentageScore()));
                row.createCell(4).setCellValue(result.getTotalQuestions());
                row.createCell(5).setCellValue(result.getCorrectAnswers());
                row.createCell(6).setCellValue(result.getTimeTakenSeconds());
                row.createCell(7).setCellValue(result.getCompletedAt().format(DATE_TIME_FORMATTER));
            }

            // Auto-size columns
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the workbook to the output stream
            workbook.write(outputStream);
        }
    }

    private void createHeaderCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}