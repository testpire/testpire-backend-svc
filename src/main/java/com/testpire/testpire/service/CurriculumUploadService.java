package com.testpire.testpire.service;

import com.testpire.testpire.dto.response.CurriculumUploadResponseDto;
import com.testpire.testpire.entity.Chapter;
import com.testpire.testpire.entity.Subject;
import com.testpire.testpire.entity.Topic;
import com.testpire.testpire.repository.ChapterRepository;
import com.testpire.testpire.repository.SubjectRepository;
import com.testpire.testpire.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses a denormalized curriculum CSV and upserts the Subject/Chapter/Topic hierarchy.
 *
 * <p>CSV format (11 columns, header required):
 * Subject Code, Subject Name, Subject Description,
 * Chapter Code, Chapter Name, Chapter Description, Chapter Order,
 * Topic Code, Topic Name, Topic Description, Topic Order
 *
 * <p>Partial rows are supported:
 * - Subject Code + Name only  → creates/reuses the subject.
 * - Chapter Code + Name added → creates/reuses chapter under that subject.
 * - Full row                  → creates/reuses topic under that chapter.
 *
 * <p>Deduplication is by code per institute: if a code already exists in the DB
 * or was created earlier in this upload, the existing entity is reused (not updated).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumUploadService {

    static final List<String> HEADERS = List.of(
            "Subject Code", "Subject Name", "Subject Description",
            "Chapter Code", "Chapter Name", "Chapter Description", "Chapter Order",
            "Topic Code", "Topic Name", "Topic Description", "Topic Order"
    );
    private static final int COLUMN_COUNT = HEADERS.size();

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;

    @Transactional
    public CurriculumUploadResponseDto processBulkUpload(MultipartFile csvFile, Long instituteId, String createdBy) {
        List<String> errors = new ArrayList<>();
        int totalRows = 0;
        int subjectsCreated = 0, chaptersCreated = 0, topicsCreated = 0;
        int subjectsReused = 0, chaptersReused = 0, topicsReused = 0;

        // Per-upload caches keyed by code (avoid N+1 for repeated codes in same file)
        Map<String, Subject> subjectCache = new HashMap<>();
        Map<String, Chapter> chapterCache = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                return fail("CSV file is empty.");
            }
            try {
                validateHeader(parseCsvLine(headerLine));
            } catch (IllegalArgumentException e) {
                return fail(e.getMessage());
            }

            int rowNumber = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                totalRows++;
                rowNumber++;

                try {
                    String[] cols = parseCsvLine(line);
                    if (cols.length < COLUMN_COUNT) {
                        // Pad with empty strings to allow partial rows
                        String[] padded = new String[COLUMN_COUNT];
                        System.arraycopy(cols, 0, padded, 0, cols.length);
                        for (int i = cols.length; i < COLUMN_COUNT; i++) padded[i] = "";
                        cols = padded;
                    }

                    String subjectCode = unquote(cols[0]);
                    String subjectName = unquote(cols[1]);
                    String subjectDesc = unquote(cols[2]);
                    String chapterCode = unquote(cols[3]);
                    String chapterName = unquote(cols[4]);
                    String chapterDesc = unquote(cols[5]);
                    String chapterOrderStr = unquote(cols[6]);
                    String topicCode = unquote(cols[7]);
                    String topicName = unquote(cols[8]);
                    String topicDesc = unquote(cols[9]);
                    String topicOrderStr = unquote(cols[10]);

                    if (subjectCode.isEmpty()) {
                        errors.add("Row " + rowNumber + ": Subject Code is required.");
                        continue;
                    }
                    if (subjectName.isEmpty()) {
                        errors.add("Row " + rowNumber + ": Subject Name is required.");
                        continue;
                    }
                    if (!chapterCode.isEmpty() && chapterName.isEmpty()) {
                        errors.add("Row " + rowNumber + ": Chapter Name is required when Chapter Code is provided.");
                        continue;
                    }
                    if (!topicCode.isEmpty() && topicName.isEmpty()) {
                        errors.add("Row " + rowNumber + ": Topic Name is required when Topic Code is provided.");
                        continue;
                    }
                    if (!topicCode.isEmpty() && chapterCode.isEmpty()) {
                        errors.add("Row " + rowNumber + ": Chapter Code is required when Topic Code is provided.");
                        continue;
                    }

                    // Subject — create-if-absent by code
                    Subject subject = subjectCache.get(subjectCode);
                    if (subject == null) {
                        subject = subjectRepository.findByCodeAndInstituteId(subjectCode, instituteId)
                                .orElse(null);
                        if (subject == null) {
                            subject = subjectRepository.save(Subject.builder()
                                    .code(subjectCode)
                                    .name(subjectName)
                                    .description(subjectDesc.isEmpty() ? null : subjectDesc)
                                    .instituteId(instituteId)
                                    .createdBy(createdBy)
                                    .build());
                            subjectsCreated++;
                        } else {
                            subjectsReused++;
                        }
                        subjectCache.put(subjectCode, subject);
                    }

                    if (chapterCode.isEmpty()) continue;

                    // Chapter — create-if-absent by code
                    String chapterCacheKey = chapterCode;
                    Chapter chapter = chapterCache.get(chapterCacheKey);
                    if (chapter == null) {
                        chapter = chapterRepository.findByCodeAndInstituteId(chapterCode, instituteId)
                                .orElse(null);
                        if (chapter == null) {
                            Integer chapterOrder = parseOptionalInt(chapterOrderStr);
                            chapter = chapterRepository.save(Chapter.builder()
                                    .code(chapterCode)
                                    .name(chapterName)
                                    .description(chapterDesc.isEmpty() ? null : chapterDesc)
                                    .subject(subject)
                                    .instituteId(instituteId)
                                    .orderIndex(chapterOrder)
                                    .createdBy(createdBy)
                                    .build());
                            chaptersCreated++;
                        } else {
                            chaptersReused++;
                        }
                        chapterCache.put(chapterCacheKey, chapter);
                    }

                    if (topicCode.isEmpty()) continue;

                    // Topic — create-if-absent by code
                    boolean topicExists = topicRepository.existsByCodeAndInstituteId(topicCode, instituteId);
                    if (!topicExists) {
                        Integer topicOrder = parseOptionalInt(topicOrderStr);
                        topicRepository.save(Topic.builder()
                                .code(topicCode)
                                .name(topicName)
                                .description(topicDesc.isEmpty() ? null : topicDesc)
                                .chapter(chapter)
                                .instituteId(instituteId)
                                .orderIndex(topicOrder)
                                .createdBy(createdBy)
                                .build());
                        topicsCreated++;
                    } else {
                        topicsReused++;
                    }

                } catch (Exception e) {
                    log.error("Error processing curriculum row {}: {}", rowNumber, e.getMessage());
                    errors.add("Row " + rowNumber + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            log.error("Error reading curriculum CSV", e);
            errors.add("Error reading CSV file: " + e.getMessage());
        }

        return CurriculumUploadResponseDto.builder()
                .totalRows(totalRows)
                .subjectsCreated(subjectsCreated)
                .chaptersCreated(chaptersCreated)
                .topicsCreated(topicsCreated)
                .subjectsReused(subjectsReused)
                .chaptersReused(chaptersReused)
                .topicsReused(topicsReused)
                .errors(errors)
                .build();
    }

    void validateHeader(String[] header) {
        if (header.length < COLUMN_COUNT) {
            throw new IllegalArgumentException(String.format(
                    "Invalid CSV header: expected %d columns but found %d. Expected: %s",
                    COLUMN_COUNT, header.length, String.join(", ", HEADERS)));
        }
        for (int i = 0; i < COLUMN_COUNT; i++) {
            String expected = HEADERS.get(i).toLowerCase().trim();
            String actual = header[i] == null ? "" : header[i].replace("\"", "").trim().toLowerCase();
            if (!expected.equals(actual)) {
                throw new IllegalArgumentException(String.format(
                        "Invalid CSV header at column %d: expected \"%s\" but found \"%s\". Expected columns: %s",
                        i + 1, HEADERS.get(i), header[i] == null ? "" : header[i].trim(),
                        String.join(", ", HEADERS)));
            }
        }
    }

    private CurriculumUploadResponseDto fail(String error) {
        return CurriculumUploadResponseDto.builder()
                .totalRows(0).subjectsCreated(0).chaptersCreated(0).topicsCreated(0)
                .subjectsReused(0).chaptersReused(0).topicsReused(0)
                .errors(List.of(error))
                .build();
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }

    private String unquote(String value) {
        return value == null ? "" : value.replace("\"", "").trim();
    }

    private Integer parseOptionalInt(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
