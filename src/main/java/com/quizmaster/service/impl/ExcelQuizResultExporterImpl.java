package com.quizmaster.service.impl;

import com.quizmaster.model.QuizResult;
import com.quizmaster.service.QuizResultExporter;
import com.quizmaster.util.LoggingUtils;
import org.slf4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for exporting quiz results to Excel
 */
@Service
public class ExcelQuizResultExporterImpl implements QuizResultExporter {

    private static final Logger log = LoggingUtils.getLogger(ExcelQuizResultExporterImpl.class);
    private static final String[] HEADERS = {"ID", "User Name", "I-Number", "Score", "Total Questions", 
                                         "Correct Answers", "Duration (sec)", "Completion Date"};
    private static final String SHEET_NAME = "Quiz Results";
    
    @Value("${results.excel-path:file:./results.xlsx}")
    private String resultsExcelPath;
    
    @Override
    public void exportResults(List<QuizResult> results) {
        try {
            String filePath = getFilePath();
            log.info("Exporting {} quiz results to Excel file: {}", results.size(), filePath);
            
            Workbook workbook;
            File file = new File(filePath);
            
            if (file.exists()) {
                // If file exists, load it
                try (FileInputStream fis = new FileInputStream(file)) {
                    workbook = new XSSFWorkbook(fis);
                    // Clear existing data except header
                    Sheet sheet = workbook.getSheet(SHEET_NAME);
                    if (sheet != null) {
                        int lastRowNum = sheet.getLastRowNum();
                        for (int i = 1; i <= lastRowNum; i++) {
                            Row row = sheet.getRow(i);
                            if (row != null) {
                                sheet.removeRow(row);
                            }
                        }
                    } else {
                        sheet = workbook.createSheet(SHEET_NAME);
                        // Create header row
                        Row headerRow = sheet.createRow(0);
                        CellStyle headerStyle = createHeaderStyle(workbook);
                        
                        for (int i = 0; i < HEADERS.length; i++) {
                            Cell cell = headerRow.createCell(i);
                            cell.setCellValue(HEADERS[i]);
                            cell.setCellStyle(headerStyle);
                        }
                    }
                }
            } else {
                // Create new workbook
                workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet(SHEET_NAME);
                
                // Create header row
                Row headerRow = sheet.createRow(0);
                CellStyle headerStyle = createHeaderStyle(workbook);
                
                for (int i = 0; i < HEADERS.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(HEADERS[i]);
                    cell.setCellStyle(headerStyle);
                }
            }
            
            // Add data rows
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            int rowNum = 1; // Start from second row (after header)
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            CellStyle dateStyle = workbook.createCellStyle();
            
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
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            
            workbook.close();
            log.info("Successfully exported quiz results to Excel file");
            
        } catch (IOException e) {
            log.error("Error exporting quiz results to Excel", e);
        }
    }
    
    @Override
    public List<QuizResult> importResults() {
        List<QuizResult> results = new ArrayList<>();
        
        try {
            String filePath = getFilePath();
            File file = new File(filePath);
            
            if (!file.exists()) {
                log.info("Results Excel file not found at: {}. Will be created when results are available.", filePath);
                return results;
            }
            
            log.info("Importing quiz results from Excel file: {}", filePath);
            
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    log.warn("Sheet '{}' not found in Excel file", SHEET_NAME);
                    return results;
                }
                
                int lastRowNum = sheet.getLastRowNum();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                
                // Skip header row (index 0)
                for (int i = 1; i <= lastRowNum; i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        QuizResult result = new QuizResult();
                        
                        // Extract cell values
                        try {
                            // ID
                            Cell idCell = row.getCell(0);
                            if (idCell != null) {
                                if (idCell.getCellType() == CellType.NUMERIC) {
                                    result.setId((long) idCell.getNumericCellValue());
                                } else {
                                    result.setId(Long.parseLong(idCell.getStringCellValue()));
                                }
                            }
                            
                            // User Name
                            Cell userNameCell = row.getCell(1);
                            if (userNameCell != null) {
                                result.setUserName(userNameCell.getStringCellValue());
                            }
                            
                            // I Number
                            Cell iNumberCell = row.getCell(2);
                            if (iNumberCell != null) {
                                result.setINumber(iNumberCell.getStringCellValue());
                            }
                            
                            // Score
                            Cell scoreCell = row.getCell(3);
                            if (scoreCell != null) {
                                result.setPercentageScore((float) scoreCell.getNumericCellValue());
                            }
                            
                            // Total Questions
                            Cell totalQuestionsCell = row.getCell(4);
                            if (totalQuestionsCell != null) {
                                result.setTotalQuestions((int) totalQuestionsCell.getNumericCellValue());
                            }
                            
                            // Correct Answers
                            Cell correctAnswersCell = row.getCell(5);
                            if (correctAnswersCell != null) {
                                result.setCorrectAnswers((int) correctAnswersCell.getNumericCellValue());
                            }
                            
                            // Duration
                            Cell durationCell = row.getCell(6);
                            if (durationCell != null) {
                                result.setTimeTakenSeconds((int) durationCell.getNumericCellValue());
                            }
                            
                            // Completion Date
                            Cell dateCell = row.getCell(7);
                            if (dateCell != null) {
                                String dateStr;
                                if (dateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dateCell)) {
                                    dateStr = dateCell.getLocalDateTimeCellValue().format(formatter);
                                } else {
                                    dateStr = dateCell.getStringCellValue();
                                }
                                result.setCompletedAt(LocalDateTime.parse(dateStr, formatter));
                            } else {
                                result.setCompletedAt(LocalDateTime.now());
                            }
                            
                            results.add(result);
                        } catch (Exception e) {
                            log.warn("Error parsing row {}: {}", i, e.getMessage());
                        }
                    }
                }
            }
            
            log.info("Successfully imported {} quiz results from Excel file", results.size());
        } catch (IOException e) {
            log.error("Error importing quiz results from Excel", e);
        }
        
        return results;
    }
    
    @Override
    public boolean createBackup() throws IOException {
        String filePath = getFilePath();
        File sourceFile = new File(filePath);
        
        if (!sourceFile.exists()) {
            log.info("No Excel file to back up at: {}", filePath);
            return false;
        }
        
        // Create backup file name with timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String backupFileName = filePath.replace(".xlsx", "_backup_" + timestamp + ".xlsx");
        
        try {
            Path sourcePath = sourceFile.toPath();
            Path backupPath = Paths.get(backupFileName);
            
            // Copy the file with replace if exists option
            Files.copy(sourcePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Successfully created backup of Excel file at: {}", backupFileName);
            return true;
        } catch (IOException e) {
            log.error("Failed to create backup of Excel file", e);
            throw e;
        }
    }
    
    private String getFilePath() throws IOException {
        String path = resultsExcelPath.replace("file:", "");
        Path filePath = Paths.get(path);
        
        // Handle parent directory to prevent NPE with getParent()
        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        return path;
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        return headerStyle;
    }
}