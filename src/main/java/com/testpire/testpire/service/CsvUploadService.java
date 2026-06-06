package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.CreateQuestionRequestDto;
import com.testpire.testpire.dto.request.CreateOptionRequestDto;
import com.testpire.testpire.dto.response.BulkUploadResponseDto;
import com.testpire.testpire.dto.response.QuestionResponseDto;
import com.testpire.testpire.entity.Institute;
import com.testpire.testpire.enums.DifficultyLevel;
import com.testpire.testpire.repository.InstituteRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvUploadService {

    /** Fixed (non-option) columns, in order, that every CSV must start with. */
    static final List<String> FIXED_HEADERS = List.of(
            "Question Id", "Question Text", "Question Image URL", "Difficulty Level", "Question Type",
            "Marks", "Negative Marks", "Explanation", "Topic ID");
    private static final int FIXED_COLUMN_COUNT = FIXED_HEADERS.size(); // 9
    private static final int OPTION_GROUP_SIZE = 3; // Text, Image URL, IsCorrect

    private static final Set<String> TRUE_VALUES = Set.of("true", "1", "yes");
    private static final Set<String> FALSE_VALUES = Set.of("false", "0", "no", "");

    private final QuestionService questionService;
    private final QuestionImageService questionImageService;
    private final InstituteRepository instituteRepository;

    public BulkUploadResponseDto processBulkUpload(MultipartFile csvFile, Long instituteId, String createdBy) {
        List<String> errors = new ArrayList<>();
        List<QuestionResponseDto> uploadedQuestions = new ArrayList<>();
        int totalProcessed = 0;
        int successfulUploads = 0;
        int failedUploads = 0;

        if (instituteId == null) {
            return failFast("No institute resolved for this upload; cannot derive question ids.");
        }
        // External question ids are prefixed with the institute code so the same CSV id (e.g. "Q01")
        // can be reused across institutes without collision. Resolve it once for the whole upload; if
        // the institute has no code on record, fall back to its numeric id.
        String institutePrefix = instituteRepository.findById(instituteId)
                .map(Institute::getCode)
                .filter(code -> code != null && !code.isBlank())
                .orElse(String.valueOf(instituteId));
        // Raw ids seen in this file, to reject duplicates within a single upload (a re-run across
        // uploads still updates the existing question — that is the intended idempotent behavior).
        Set<String> seenRawIds = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return failFast("CSV file is empty");
            }
            try {
                validateHeader(parseCsvLine(headerLine));
            } catch (IllegalArgumentException e) {
                return failFast(e.getMessage());
            }

            int rowNumber = 1; // data rows are numbered from 1 (header excluded)
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue; // skip blank lines without counting them
                }
                totalProcessed++;
                rowNumber++;

                try {
                    String[] columns = parseCsvLine(line);
                    CreateQuestionRequestDto questionRequest = createQuestionFromCsvRow(
                            columns, instituteId, institutePrefix, seenRawIds, createdBy, rowNumber, errors);
                    QuestionResponseDto question = questionService.createQuestion(questionRequest);
                    uploadedQuestions.add(question);
                    successfulUploads++;
                } catch (Exception e) {
                    log.error("Error processing row {}: {}", rowNumber, e.getMessage());
                    errors.add(String.format("Row %d: %s", rowNumber, e.getMessage()));
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

    private BulkUploadResponseDto failFast(String error) {
        return BulkUploadResponseDto.builder()
                .totalProcessed(0)
                .successfulUploads(0)
                .failedUploads(0)
                .errors(List.of(error))
                .uploadedQuestions(List.of())
                .build();
    }

    /**
     * Validates the header so column drift (reordering, missing Topic ID, wrong option grouping) fails
     * the whole upload with a clear message instead of silently mis-mapping every row.
     */
    void validateHeader(String[] header) {
        if (header.length < FIXED_COLUMN_COUNT + OPTION_GROUP_SIZE) {
            throw new IllegalArgumentException(String.format(
                    "Invalid CSV header: expected at least %d columns (%d fixed + one option group of %d), found %d. "
                            + "Expected fixed columns in order: %s, then repeating (Option Text, Option Image URL, Option IsCorrect).",
                    FIXED_COLUMN_COUNT + OPTION_GROUP_SIZE, FIXED_COLUMN_COUNT, OPTION_GROUP_SIZE,
                    header.length, String.join(", ", FIXED_HEADERS)));
        }
        for (int i = 0; i < FIXED_COLUMN_COUNT; i++) {
            String expected = normalizeHeader(FIXED_HEADERS.get(i));
            String actual = normalizeHeader(header[i]);
            if (!expected.equals(actual)) {
                throw new IllegalArgumentException(String.format(
                        "Invalid CSV header at column %d: expected \"%s\" but found \"%s\". "
                                + "Fixed columns must be in order: %s.",
                        i + 1, FIXED_HEADERS.get(i), header[i].trim(), String.join(", ", FIXED_HEADERS)));
            }
        }
        int optionColumns = header.length - FIXED_COLUMN_COUNT;
        if (optionColumns % OPTION_GROUP_SIZE != 0) {
            throw new IllegalArgumentException(String.format(
                    "Invalid CSV header: %d option column(s) after the fixed columns do not form complete groups of %d "
                            + "(Option Text, Option Image URL, Option IsCorrect).",
                    optionColumns, OPTION_GROUP_SIZE));
        }
    }

    private String normalizeHeader(String value) {
        return value == null ? "" : value.replace("\"", "").trim().toLowerCase().replaceAll("\\s+", " ");
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

    /**
     * Validates and builds one question from a CSV row. All field validation runs first and collects
     * specific messages; if any field is invalid the row is rejected (no images are uploaded). Image
     * fetch failures are non-fatal and recorded as warnings in {@code warnings}.
     */
    private CreateQuestionRequestDto createQuestionFromCsvRow(
            String[] columns, Long instituteId, String institutePrefix, Set<String> seenRawIds,
            String createdBy, int rowNumber, List<String> warnings) {
        List<String> rowErrors = new ArrayList<>();

        if (columns.length < FIXED_COLUMN_COUNT + OPTION_GROUP_SIZE) {
            throw new IllegalArgumentException(String.format(
                    "Insufficient columns: found %d, expected at least %d (%d fixed + one option group).",
                    columns.length, FIXED_COLUMN_COUNT + OPTION_GROUP_SIZE, FIXED_COLUMN_COUNT));
        }
        int optionColumns = columns.length - FIXED_COLUMN_COUNT;
        if (optionColumns % OPTION_GROUP_SIZE != 0) {
            rowErrors.add(String.format(
                    "%d trailing column(s) do not form a complete option group of %d (Text, Image URL, IsCorrect).",
                    optionColumns % OPTION_GROUP_SIZE, OPTION_GROUP_SIZE));
        }

        // Column 0 is the caller-supplied Question Id; it is prefixed with the institute code (or the
        // institute id, if no code) to form the stored external id, which drives idempotent re-uploads.
        String rawQuestionId = unquote(columns[0]);
        String externalId = null;
        if (rawQuestionId.isEmpty()) {
            rowErrors.add("Question Id is required.");
        } else if (!seenRawIds.add(rawQuestionId)) {
            rowErrors.add("Duplicate Question Id \"" + rawQuestionId + "\" within this file.");
        } else {
            externalId = institutePrefix + "_" + rawQuestionId;
        }

        String questionText = unquote(columns[1]);
        String questionImageUrl = unquote(columns[2]);
        String difficultyStr = unquote(columns[3]);
        String questionType = unquote(columns[4]);
        String explanation = unquote(columns[7]);

        if (questionText.isEmpty()) {
            rowErrors.add("Question Text is required.");
        }

        DifficultyLevel difficultyLevel = null;
        if (difficultyStr.isEmpty()) {
            rowErrors.add("Difficulty Level is required (one of " + Arrays.toString(DifficultyLevel.values()) + ").");
        } else {
            try {
                difficultyLevel = DifficultyLevel.valueOf(difficultyStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                rowErrors.add("Invalid Difficulty Level \"" + difficultyStr + "\". Allowed: "
                        + Arrays.toString(DifficultyLevel.values()) + ".");
            }
        }

        if (questionType.isEmpty()) {
            rowErrors.add("Question Type is required.");
        }

        Integer marks = parseIntField(unquote(columns[5]), "Marks", 1, rowErrors);
        Integer negativeMarks = parseIntField(unquote(columns[6]), "Negative Marks", 0, rowErrors);

        Long topicIdLong = null;
        String topicIdStr = unquote(columns[8]);
        if (topicIdStr.isEmpty()) {
            rowErrors.add("Topic ID is required.");
        } else {
            try {
                topicIdLong = Long.parseLong(topicIdStr);
            } catch (NumberFormatException e) {
                rowErrors.add("Topic ID must be a number, found \"" + topicIdStr + "\".");
            }
        }

        // Parse + validate options before any image upload.
        List<ParsedOption> parsedOptions = new ArrayList<>();
        int correctCount = 0;
        for (int i = FIXED_COLUMN_COUNT; i + OPTION_GROUP_SIZE - 1 < columns.length; i += OPTION_GROUP_SIZE) {
            String optionText = unquote(columns[i]);
            String optionImageUrl = unquote(columns[i + 1]);
            String isCorrectRaw = unquote(columns[i + 2]);
            int optionNumber = (i - FIXED_COLUMN_COUNT) / OPTION_GROUP_SIZE + 1;

            boolean hasContent = !optionText.isEmpty() || !optionImageUrl.isEmpty() || !isCorrectRaw.isEmpty();
            if (!hasContent) {
                continue; // entirely empty trailing option group
            }
            if (optionText.isEmpty()) {
                rowErrors.add("Option " + optionNumber + " is missing text.");
                continue;
            }
            Boolean isCorrect = parseBoolean(isCorrectRaw);
            if (isCorrect == null) {
                rowErrors.add("Option " + optionNumber + " IsCorrect must be true/false/1/0/yes/no, found \""
                        + isCorrectRaw + "\".");
                continue;
            }
            if (isCorrect) {
                correctCount++;
            }
            parsedOptions.add(new ParsedOption(optionText, optionImageUrl, isCorrect));
        }

        if (parsedOptions.size() < 2) {
            rowErrors.add("At least two options are required.");
        }
        if (correctCount == 0 && !parsedOptions.isEmpty()) {
            rowErrors.add("At least one option must be marked correct.");
        }

        if (!rowErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", rowErrors));
        }

        // Validation passed — now perform the (non-fatal) image uploads.
        List<CreateOptionRequestDto> options = new ArrayList<>();
        int optionNumber = 0;
        for (ParsedOption po : parsedOptions) {
            optionNumber++;
            String optionImagePath = "";
            if (!po.imageUrl().isEmpty()) {
                try {
                    optionImagePath = questionImageService.uploadFromUrl(topicIdLong, po.imageUrl(), true);
                } catch (Exception e) {
                    warnings.add(String.format("Row %d (warning): option %d image upload failed: %s",
                            rowNumber, optionNumber, e.getMessage()));
                }
            }
            options.add(CreateOptionRequestDto.builder()
                    .text(po.text())
                    .optionImagePath(optionImagePath)
                    .isCorrect(po.isCorrect())
                    .build());
        }

        String questionImagePath = "";
        if (!questionImageUrl.isEmpty()) {
            try {
                questionImagePath = questionImageService.uploadFromUrl(topicIdLong, questionImageUrl, false);
            } catch (Exception e) {
                warnings.add(String.format("Row %d (warning): question image upload failed: %s",
                        rowNumber, e.getMessage()));
            }
        }

        return CreateQuestionRequestDto.builder()
                .text(questionText)
                .externalId(externalId)
                .questionImagePath(questionImagePath)
                .difficultyLevel(difficultyLevel)
                .instituteId(instituteId)
                .questionType(questionType)
                .marks(marks)
                .topicId(topicIdLong)
                .negativeMarks(negativeMarks)
                .explanation(explanation)
                .options(options)
                .build();
    }

    private String unquote(String value) {
        return value == null ? "" : value.replace("\"", "").trim();
    }

    /** Empty -> default; present-but-invalid -> records an error and returns the default. */
    private Integer parseIntField(String value, String fieldName, int defaultValue, List<String> rowErrors) {
        if (value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            rowErrors.add(fieldName + " must be a whole number, found \"" + value + "\".");
            return defaultValue;
        }
    }

    /** Returns Boolean for recognized values, null for unrecognized (so callers can flag it). */
    private Boolean parseBoolean(String value) {
        String v = value.toLowerCase();
        if (TRUE_VALUES.contains(v)) {
            return true;
        }
        if (FALSE_VALUES.contains(v)) {
            return false;
        }
        return null;
    }

    private record ParsedOption(String text, String imageUrl, boolean isCorrect) {}
}
