
package com.quizmaster.repository.impl;

import com.quizmaster.model.QuizResult;
import com.quizmaster.repository.impl.QuizResultRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class FileQuizResultRepository implements QuizResultRepository {

    private static final String EXCEL_FILE_NAME = "quiz_results.xlsx";
    private static final String CSV_FILE_NAME = "quiz_results.csv";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${quizmaster.results.storage-type:excel}")
    private String storageType;

    @Value("${quizmaster.results.file-path:./quiz_results.xlsx}")
    private String configuredFilePath;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private String getFilePath() {
        if (configuredFilePath != null && !configuredFilePath.isEmpty()) {
            return configuredFilePath;
        }

        String fileName = "excel".equalsIgnoreCase(storageType) ? EXCEL_FILE_NAME : CSV_FILE_NAME;
        return System.getProperty("user.dir") + File.separator + fileName;
    }

    @Override
    public void saveResult(QuizResult result) {
        if ("excel".equalsIgnoreCase(storageType)) {
            saveToExcel(result);
        } else {
            saveToCsv(result);
        }
    }

    @Override
    public List<QuizResult> getAllResults() {
        if ("excel".equalsIgnoreCase(storageType)) {
            return readFromExcel();
        } else {
            return readFromCsv();
        }
    }

    @Override
    public void clearAllResults() throws IOException {
        try {
            lock.writeLock().lock();
            File file = new File(getFilePath());
            if (file.exists()) {
                file.delete();
            }

            if ("excel".equalsIgnoreCase(storageType)) {
                createExcelFile();
            } else {
                createCsvFile();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Excel implementation
    private void saveToExcel(QuizResult result) {
        try {
            lock.writeLock().lock();

            String filePath = getFilePath();
            File file = new File(filePath);

            Workbook workbook;
            Sheet sheet;

            if (!file.exists()) {
                workbook = createExcelFile();
                sheet = workbook.getSheetAt(0);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    workbook = WorkbookFactory.create(fis);
                    sheet = workbook.getSheetAt(0);
                }
            }

            int lastRowNum = sheet.getLastRowNum();
            Row row = sheet.createRow(lastRowNum + 1);

            row.createCell(0).setCellValue(result.getUserName());
            row.createCell(1).setCellValue(result.getScore());
            row.createCell(2).setCellValue(result.getPercentageScore());
            row.createCell(3).setCellValue(result.getTotalQuestions());
            row.createCell(4).setCellValue(result.getCorrectAnswers());
            row.createCell(5).setCellValue(result.getTimeTakenSeconds());
            row.createCell(6).setCellValue(result.getCompletedAt().format(DATE_TIME_FORMATTER));

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

            workbook.close();

        } catch (Exception e) {
            throw new RuntimeException("Error saving quiz result to Excel", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private List<QuizResult> readFromExcel() {
        List<QuizResult> results = new ArrayList<>();

        try {
            lock.readLock().lock();

            File file = new File(getFilePath());
            if (!file.exists()) {
                createExcelFile();
                return results;
            }

            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = WorkbookFactory.create(fis)) {

                Sheet sheet = workbook.getSheetAt(0);

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    QuizResult result = new QuizResult();
                    result.setUserName(getCellValueAsString(row.getCell(0)));
                    result.setScore((int) row.getCell(1).getNumericCellValue());
                    result.setPercentageScore(row.getCell(2).getNumericCellValue());
                    result.setTotalQuestions((int) row.getCell(3).getNumericCellValue());
                    result.setCorrectAnswers((int) row.getCell(4).getNumericCellValue());
                    result.setTimeTakenSeconds((int) row.getCell(5).getNumericCellValue());
                    result.setCompletedAt(LocalDateTime.parse(
                            getCellValueAsString(row.getCell(6)), DATE_TIME_FORMATTER));

                    results.add(result);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading quiz results from Excel", e);
        } finally {
            lock.readLock().unlock();
        }

        return results;
    }

    private Workbook createExcelFile() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Quiz Results");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("User Name");
        headerRow.createCell(1).setCellValue("Score");
        headerRow.createCell(2).setCellValue("Percentage Score");
        headerRow.createCell(3).setCellValue("Total Questions");
        headerRow.createCell(4).setCellValue("Correct Answers");
        headerRow.createCell(5).setCellValue("Time Taken (seconds)");
        headerRow.createCell(6).setCellValue("Completed At");

        // Create the file
        try (FileOutputStream fos = new FileOutputStream(getFilePath())) {
            workbook.write(fos);
        }

        return workbook;
    }

    // CSV implementation
    private void saveToCsv(QuizResult result) {
        try {
            lock.writeLock().lock();

            String filePath = getFilePath();
            File file = new File(filePath);

            if (!file.exists()) {
                createCsvFile();
            }

            try (FileWriter fw = new FileWriter(file, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {

                String line = String.format("%s,%d,%.2f,%d,%d,%d,%s\n",
                        result.getUserName(),
                        result.getScore(),
                        result.getPercentageScore(),
                        result.getTotalQuestions(),
                        result.getCorrectAnswers(),
                        result.getTimeTakenSeconds(),
                        result.getCompletedAt().format(DATE_TIME_FORMATTER));

                bw.write(line);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error saving quiz result to CSV", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private List<QuizResult> readFromCsv() {
        List<QuizResult> results = new ArrayList<>();

        try {
            lock.readLock().lock();

            File file = new File(getFilePath());
            if (!file.exists()) {
                createCsvFile();
                return results;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                // Skip header
                String line = br.readLine();

                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length < 7) continue;

                    QuizResult result = new QuizResult();
                    result.setUserName(values[0]);
                    result.setScore(Integer.parseInt(values[1]));
                    result.setPercentageScore(Double.parseDouble(values[2]));
                    result.setTotalQuestions(Integer.parseInt(values[3]));
                    result.setCorrectAnswers(Integer.parseInt(values[4]));
                    result.setTimeTakenSeconds(Integer.parseInt(values[5]));
                    result.setCompletedAt(LocalDateTime.parse(values[6], DATE_TIME_FORMATTER));

                    results.add(result);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading quiz results from CSV", e);
        } finally {
            lock.readLock().unlock();
        }

        return results;
    }

    private void createCsvFile() throws IOException {
        Path path = Paths.get(getFilePath());
        Files.createDirectories(path.getParent());

        try (FileWriter fw = new FileWriter(path.toFile());
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write("User Name,Score,Percentage Score,Total Questions,Correct Answers,Time Taken (seconds),Completed At\n");
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DATE_TIME_FORMATTER);
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}