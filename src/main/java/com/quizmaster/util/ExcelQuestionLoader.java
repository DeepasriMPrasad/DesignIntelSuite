package com.quizmaster.util;

import com.quizmaster.exception.QuizException;
import com.quizmaster.model.Option;
import com.quizmaster.model.Question;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelQuestionLoader {
    
    private static final String EXCEL_FILE_PATH = "questions.xlsx";
    
    /**
     * Loads questions from the Excel file
     * 
     * @return A list of questions with options
     */
    public List<Question> loadQuestions() {
        List<Question> questions = new ArrayList<>();
        
        try (InputStream is = new ClassPathResource(EXCEL_FILE_PATH).getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
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
