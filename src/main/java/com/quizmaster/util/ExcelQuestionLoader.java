package com.quizmaster.util;

import com.quizmaster.exception.QuizException;
import com.quizmaster.model.Option;
import com.quizmaster.model.Question;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelQuestionLoader {
    
    @Value("${questions.excel-path:file:./questions.xlsx}")
    private Resource questionsExcelResource;
    
    /**
     * Loads questions from the Excel file in the same directory as the JAR file
     * If the file doesn't exist, will fall back to classpath resource
     * 
     * @return A list of questions with options
     */
    public List<Question> loadQuestions() {
        List<Question> questions = new ArrayList<>();
        
        try {
            // Try to load from external file first
            File externalFile = null;
            Workbook workbook = null;
            
            try {
                externalFile = questionsExcelResource.getFile();
                if (externalFile.exists()) {
                    // External file exists, use it
                    workbook = WorkbookFactory.create(new FileInputStream(externalFile));
                }
            } catch (IOException e) {
                // If external file can't be accessed, fall back to classpath
                workbook = null;
            }
            
            // If external file doesn't exist or couldn't be loaded, try classpath resource
            if (workbook == null) {
                try (InputStream is = new ClassPathResource("questions.xlsx").getInputStream()) {
                    workbook = new XSSFWorkbook(is);
                }
            }
            
            if (workbook != null) {
                Sheet sheet = workbook.getSheetAt(0);
                
                // Skip the header row
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    String questionId = getCellStringValue(row, 0);
                    String questionText = getCellStringValue(row, 1);
                    
                    if (questionText == null || questionText.isEmpty()) continue;
                    
                    // Create options based on our Excel format (option ID, text, correct flag for each option)
                    List<Option> options = new ArrayList<>();
                    
                    // Process 4 options (3 columns each: ID, Text, Correct flag)
                    for (int optionIdx = 0; optionIdx < 4; optionIdx++) {
                        int baseCol = 2 + (optionIdx * 3); // Starting from column 2 (Option1_ID)
                        
                        String optionId = getCellStringValue(row, baseCol);
                        String optionText = getCellStringValue(row, baseCol + 1);
                        boolean isCorrect = isCellValueTrue(row, baseCol + 2);
                        
                        if (optionText != null && !optionText.isEmpty()) {
                            options.add(Option.builder()
                                    .id(optionId)
                                    .text(optionText)
                                    .correct(isCorrect)
                                    .build());
                        }
                    }
                    
                    if (options.size() != 4) {
                        throw new QuizException("Question must have exactly 4 options: " + questionText);
                    }
                    
                    if (options.stream().noneMatch(Option::isCorrect)) {
                        throw new QuizException("Question must have at least one correct answer: " + questionText);
                    }
                    
                    questions.add(Question.builder()
                            .id(questionId)
                            .text(questionText)
                            .options(options)
                            .build());
                }
                
                // Close the workbook
                try {
                    workbook.close();
                } catch (IOException e) {
                    // Ignore close errors
                }
            }
        } catch (IOException e) {
            throw new QuizException("Error loading questions from Excel file", e);
        }
        
        if (questions.isEmpty()) {
            throw new QuizException("No questions found in the Excel file");
        }
        
        return questions;
    }
    
    private String getCellStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
    
    private boolean isCellValueTrue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return false;
        
        switch (cell.getCellType()) {
            case STRING:
                String value = cell.getStringCellValue();
                return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equals(value);
            case NUMERIC:
                return cell.getNumericCellValue() == 1;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            default:
                return false;
        }
    }
}
