package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.CreateQuestionRequestDto;
import com.testpire.testpire.dto.request.CreateOptionRequestDto;
import com.testpire.testpire.dto.response.BulkUploadResponseDto;
import com.testpire.testpire.dto.response.QuestionResponseDto;
import com.testpire.testpire.enums.DifficultyLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvUploadService {

    private final QuestionService questionService;
    private final S3Service s3Service;

    public BulkUploadResponseDto processBulkUpload(MultipartFile csvFile, Long instituteId, String createdBy) {
        List<String> errors = new ArrayList<>();
        List<QuestionResponseDto> uploadedQuestions = new ArrayList<>();
        int totalProcessed = 0;
        int successfulUploads = 0;
        int failedUploads = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header row
                }
                
                totalProcessed++;
                
                try {
                    String[] columns = parseCsvLine(line);
                    if (columns.length < 8) {
                        errors.add(String.format("Row %d: Insufficient columns. Expected at least 8 columns.", totalProcessed));
                        failedUploads++;
                        continue;
                    }

                    CreateQuestionRequestDto questionRequest = createQuestionFromCsvRow(columns,  instituteId, createdBy);
                    QuestionResponseDto question = questionService.createQuestion(questionRequest);
                    uploadedQuestions.add(question);
                    successfulUploads++;
                    
                } catch (Exception e) {
                    log.error("Error processing row {}: {}", totalProcessed, e.getMessage());
                    errors.add(String.format("Row %d: %s", totalProcessed, e.getMessage()));
                    failedUploads++;
                }
            }
            
        } catch (IOException e) {
            log.error("Error reading CSV file", e);
            errors.add("Error reading CSV file: " + e.getMessage());
        }

        return BulkUploadResponseDto.builder()
                .totalProcessed(totalProcessed)
                .successfulUploads(successfulUploads)
                .failedUploads(failedUploads)
                .errors(errors)
                .uploadedQuestions(uploadedQuestions)
                .build();
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        result.add(currentField.toString().trim());
        return result.toArray(new String[0]);
    }

    private CreateQuestionRequestDto createQuestionFromCsvRow(String[] columns, Long instituteId, String createdBy) throws Exception {
        // CSV Format: Question Text, Question Image URL, Difficulty Level, Question Type, Marks, Negative Marks, Explanation, Option1 Text, Option1 Image URL, Option1 IsCorrect, Option2 Text, Option2 Image URL, Option2 IsCorrect, ...
        
        String questionText = columns[0].replaceAll("\"", "");
        String questionImageUrl = columns.length > 1 ? columns[1].replaceAll("\"", "") : "";
        String difficultyLevelStr = columns.length > 2 ? columns[2].replaceAll("\"", "").toUpperCase() : "MEDIUM";
        String questionType = columns.length > 3 ? columns[3].replaceAll("\"", "") : "MCQ";
        String marksStr = columns.length > 4 ? columns[4].replaceAll("\"", "") : "1";
        String negativeMarksStr = columns.length > 5 ? columns[5].replaceAll("\"", "") : "0";
        String explanation = columns.length > 6 ? columns[6].replaceAll("\"", "") : "";

        // Parse difficulty level
        DifficultyLevel difficultyLevel;
        try {
            difficultyLevel = DifficultyLevel.valueOf(difficultyLevelStr);
        } catch (IllegalArgumentException e) {
            difficultyLevel = DifficultyLevel.MEDIUM;
        }

        // Parse marks
        int marks = 1;
        try {
            marks = Integer.parseInt(marksStr);
        } catch (NumberFormatException e) {
            // Use default value
        }

        // Parse negative marks
        int negativeMarks = 0;
        try {
            negativeMarks = Integer.parseInt(negativeMarksStr);
        } catch (NumberFormatException e) {
            // Use default value
        }

        // Process options (starting from column 7)
        List<CreateOptionRequestDto> options = new ArrayList<>();
        int optionIndex = 1;
        
        for (int i = 7; i < columns.length; i += 3) {
            if (i + 2 < columns.length) {
                String optionText = columns[i].replaceAll("\"", "");
                String optionImageUrl = columns[i + 1].replaceAll("\"", "");
                String isCorrectStr = columns[i + 2].replaceAll("\"", "").toLowerCase();
                
                if (!optionText.isEmpty()) {
                    // Upload option image to S3 if URL provided
                    String optionImagePath = "";
                    if (!optionImageUrl.isEmpty()) {
                        try {
                            optionImagePath = s3Service.uploadImageFromUrl(optionImageUrl, 
                                    "questions/options", "option_" + optionIndex);
                        } catch (Exception e) {
                            log.warn("Failed to upload option image: {}", e.getMessage());
                        }
                    }
                    
                    options.add(CreateOptionRequestDto.builder()
                            .text(optionText)
                            .optionImagePath(optionImagePath)
                            .isCorrect("true".equals(isCorrectStr) || "1".equals(isCorrectStr) || "yes".equals(isCorrectStr))
                            .build());
                    optionIndex++;
                }
            }
        }

        // Upload question image to S3 if URL provided
        String questionImagePath = "";
        if (!questionImageUrl.isEmpty()) {
            try {
                questionImagePath = s3Service.uploadImageFromUrl(questionImageUrl, 
                        "questions", "question");
            } catch (Exception e) {
                log.warn("Failed to upload question image: {}", e.getMessage());
            }
        }

        return CreateQuestionRequestDto.builder()
                .text(questionText)
                .questionImagePath(questionImagePath)
                .difficultyLevel(difficultyLevel)
                .instituteId(instituteId)
                .questionType(questionType)
                .marks(marks)
                .negativeMarks(negativeMarks)
                .explanation(explanation)
                .options(options)
                .build();
    }
}

