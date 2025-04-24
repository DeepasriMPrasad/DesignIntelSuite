package com.quizmaster.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.quizmaster.model.Option;
import com.quizmaster.model.Question;
import com.quizmaster.service.QuestionEditorService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QuestionEditorServiceImpl implements QuestionEditorService {

    @Value("${questions.excel-path:file:./questions.xlsx}")
    private Resource questionsExcelResource;
    
    private List<Question> questions = new ArrayList<>();
    private Map<String, Question> questionMap = new HashMap<>();
    private AtomicInteger nextId = new AtomicInteger(1);
    
    @PostConstruct
    public void init() {
        try {
            // Check if external questions.xlsx exists, if not copy from classpath
            File externalFile = null;
            try {
                externalFile = questionsExcelResource.getFile();
                if (!externalFile.exists()) {
                    copyDefaultQuestionsToExternalFile(externalFile);
                }
            } catch (IOException e) {
                log.warn("Could not access external questions file, will create a new one", e);
                // If file doesn't exist or can't be accessed, create a directory and a new file
                if (externalFile != null) {
                    externalFile.getParentFile().mkdirs();
                    createNewQuestionsFile(externalFile);
                }
            }
            
            loadQuestionsFromExcel();
        } catch (IOException e) {
            log.error("Error loading questions from Excel", e);
        }
    }
    
    private void copyDefaultQuestionsToExternalFile(File externalFile) {
        log.info("External questions file not found. Creating from default template.");
        try {
            // Get the default questions from classpath
            Resource defaultResource = new ClassPathResource("questions.xlsx");
            if (defaultResource.exists()) {
                try (InputStream is = defaultResource.getInputStream();
                     FileOutputStream fos = new FileOutputStream(externalFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                log.info("Default questions copied to: {}", externalFile.getAbsolutePath());
            } else {
                log.warn("Default questions file not found in classpath, creating new file");
                createNewQuestionsFile(externalFile);
            }
        } catch (IOException e) {
            log.error("Failed to copy default questions", e);
            createNewQuestionsFile(externalFile);
        }
    }
    
    private void createNewQuestionsFile(File file) {
        log.info("Creating new questions file at: {}", file.getAbsolutePath());
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Questions");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Question", 
                                "Option1_ID", "Option1_Text", "Option1_Correct", 
                                "Option2_ID", "Option2_Text", "Option2_Correct",
                                "Option3_ID", "Option3_Text", "Option3_Correct",
                                "Option4_ID", "Option4_Text", "Option4_Correct"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Create a sample question
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("1");
            row.createCell(1).setCellValue("What is CXS?");
            
            row.createCell(2).setCellValue("1");
            row.createCell(3).setCellValue("CXS Architecture Team");
            row.createCell(4).setCellValue(true);
            
            row.createCell(5).setCellValue("2");
            row.createCell(6).setCellValue("Customer Experience Service");
            row.createCell(7).setCellValue(false);
            
            row.createCell(8).setCellValue("3");
            row.createCell(9).setCellValue("Cloud X Solutions");
            row.createCell(10).setCellValue(false);
            
            row.createCell(11).setCellValue("4");
            row.createCell(12).setCellValue("Content Xchange System");
            row.createCell(13).setCellValue(false);
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write the output to file
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            
            log.info("New questions file created successfully");
        } catch (IOException e) {
            log.error("Failed to create new questions file", e);
        }
    }
    
    private void loadQuestionsFromExcel() throws IOException {
        questions.clear();
        questionMap.clear();
        
        File externalFile = questionsExcelResource.getFile();
        if (!externalFile.exists()) {
            log.warn("Questions file does not exist at: {}", externalFile.getAbsolutePath());
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(externalFile);
             Workbook workbook = WorkbookFactory.create(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                // Create the question object
                Question question = new Question();
                String id = String.valueOf(i);
                question.setId(id);
                
                // Set question text - in column B (index 1)
                Cell questionCell = row.getCell(1);
                if (questionCell != null) {
                    question.setText(questionCell.getStringCellValue());
                }
                
                List<Option> options = new ArrayList<>();
                
                // Process 4 options (3 columns each: ID, Text, Correct flag)
                for (int optionIdx = 0; optionIdx < 4; optionIdx++) {
                    int baseCol = 2 + (optionIdx * 3); // Starting from column 2 (Option1_ID)
                    
                    // Get option ID from column
                    Cell optionIdCell = row.getCell(baseCol);
                    String optionIdStr = String.valueOf(optionIdx + 1);
                    if (optionIdCell != null) {
                        if (optionIdCell.getCellType() == CellType.NUMERIC) {
                            optionIdStr = String.valueOf((int) optionIdCell.getNumericCellValue());
                        } else if (optionIdCell.getCellType() == CellType.STRING) {
                            optionIdStr = optionIdCell.getStringCellValue();
                        }
                    }
                    
                    // Get option text from next column
                    Cell optionTextCell = row.getCell(baseCol + 1);
                    String optionText = "";
                    if (optionTextCell != null) {
                        optionText = optionTextCell.getStringCellValue();
                    }
                    
                    // Get if option is correct from next column
                    Cell correctCell = row.getCell(baseCol + 2);
                    boolean isCorrect = false;
                    if (correctCell != null) {
                        if (correctCell.getCellType() == CellType.BOOLEAN) {
                            isCorrect = correctCell.getBooleanCellValue();
                        } else if (correctCell.getCellType() == CellType.STRING) {
                            String value = correctCell.getStringCellValue();
                            isCorrect = "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equals(value);
                        } else if (correctCell.getCellType() == CellType.NUMERIC) {
                            isCorrect = correctCell.getNumericCellValue() == 1;
                        }
                    }
                    
                    if (!optionText.isEmpty()) {
                        Option option = Option.builder()
                                .id(optionIdStr)
                                .text(optionText)
                                .correct(isCorrect)
                                .build();
                        options.add(option);
                    }
                }
                
                question.setOptions(options);
                
                questions.add(question);
                questionMap.put(id, question);
                nextId.set(Math.max(nextId.get(), Integer.parseInt(id) + 1));
            }
            
            log.info("Loaded {} questions from Excel", questions.size());
        }
    }

    @Override
    public List<Question> getAllQuestions() {
        return new ArrayList<>(questions);
    }

    @Override
    public Question getQuestionById(String id) {
        return questionMap.get(id);
    }

    @Override
    public Question updateQuestion(Question question) throws IOException {
        questionMap.put(question.getId(), question);
        
        // Update the question in the list
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getId().equals(question.getId())) {
                questions.set(i, question);
                break;
            }
        }
        
        // Save changes to Excel
        saveQuestionsToExcel();
        
        return question;
    }

    @Override
    public Question addQuestion(Question question) throws IOException {
        // Assign a new ID
        String id = String.valueOf(nextId.getAndIncrement());
        question.setId(id);
        
        questions.add(question);
        questionMap.put(id, question);
        
        // Save changes to Excel
        saveQuestionsToExcel();
        
        return question;
    }

    @Override
    public void deleteQuestion(String id) throws IOException {
        Question question = questionMap.remove(id);
        if (question != null) {
            questions.removeIf(q -> q.getId().equals(id));
            
            // Save changes to Excel
            saveQuestionsToExcel();
        }
    }

    @Override
    public void saveQuestionsToExcel() throws IOException {
        File excelFile = questionsExcelResource.getFile();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Questions");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("QID");
            headerRow.createCell(1).setCellValue("Question");
            
            // Option headers (ID, Text, Correct for each option)
            for (int i = 0; i < 4; i++) {
                int baseCol = 2 + (i * 3);
                headerRow.createCell(baseCol).setCellValue("Option" + (i+1) + "_ID");
                headerRow.createCell(baseCol + 1).setCellValue("Option" + (i+1) + "_Text");
                headerRow.createCell(baseCol + 2).setCellValue("Option" + (i+1) + "_Correct");
            }
            
            // Create data rows
            int rowNum = 1;
            for (Question question : questions) {
                Row row = sheet.createRow(rowNum++);
                
                // Question ID
                row.createCell(0).setCellValue(question.getId());
                
                // Question text
                row.createCell(1).setCellValue(question.getText());
                
                // Options
                List<Option> options = question.getOptions();
                for (int i = 0; i < options.size() && i < 4; i++) {
                    Option option = options.get(i);
                    int baseCol = 2 + (i * 3);
                    
                    // Option ID
                    row.createCell(baseCol).setCellValue(option.getId());
                    
                    // Option text
                    row.createCell(baseCol + 1).setCellValue(option.getText());
                    
                    // Option correct flag
                    row.createCell(baseCol + 2).setCellValue(option.isCorrect());
                }
            }
            
            // Autosize columns
            for (int i = 0; i < 14; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                workbook.write(fileOut);
            }
            
            log.info("Saved {} questions to Excel file", questions.size());
            
            // Reload questions to ensure consistency
            loadQuestionsFromExcel();
        }
    }
}