package com.testpire.testpire.service;

import com.testpire.testpire.dto.response.BulkUploadResponseDto;
import com.testpire.testpire.dto.response.QuestionResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CsvUploadServiceTest {

    private static final String HEADER =
            "Question Text,Question Image URL,Difficulty Level,Question Type,Marks,Negative Marks,Explanation,Topic ID,"
                    + "Option1 Text,Option1 Image URL,Option1 IsCorrect,Option2 Text,Option2 Image URL,Option2 IsCorrect";

    @Mock
    private QuestionService questionService;
    @Mock
    private QuestionImageService questionImageService;

    @InjectMocks
    private CsvUploadService service;

    private BulkUploadResponseDto upload(String csv) {
        lenient().when(questionService.createQuestion(any()))
                .thenReturn(QuestionResponseDto.builder().id(1L).build());
        MockMultipartFile file = new MockMultipartFile(
                "file", "q.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        return service.processBulkUpload(file, 1L, "tester");
    }

    // ---- header validation ----

    @Test
    void acceptsValidHeader() {
        service.validateHeader(HEADER.split(","));
    }

    @Test
    void rejectsReorderedHeader() {
        String[] swapped = ("Difficulty Level,Question Image URL,Question Text,Question Type,Marks,Negative Marks,"
                + "Explanation,Topic ID,O1,O1i,O1c,O2,O2i,O2c").split(",");
        assertThatThrownBy(() -> service.validateHeader(swapped))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("column 1");
    }

    @Test
    void rejectsHeaderMissingTopicId() {
        // 8 fixed-position columns but Topic ID replaced by an option column => name mismatch at col 8
        String[] noTopic = ("Question Text,Question Image URL,Difficulty Level,Question Type,Marks,Negative Marks,"
                + "Explanation,Option1 Text,Option1 Image URL,Option1 IsCorrect,Option2 Text").split(",");
        assertThatThrownBy(() -> service.validateHeader(noTopic))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Topic ID");
    }

    @Test
    void rejectsIncompleteOptionGroupsInHeader() {
        String[] header = (HEADER + ",DanglingColumn").split(",");
        assertThatThrownBy(() -> service.validateHeader(header))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("complete groups");
    }

    @Test
    void failFastOnBadHeaderImportsNothing() {
        String csv = "Wrong,Header,Row\n\"Q\",\"\",\"EASY\"";
        BulkUploadResponseDto result = upload(csv);
        assertThat(result.totalProcessed()).isZero();
        assertThat(result.errors()).anyMatch(e -> e.contains("Invalid CSV header"));
    }

    // ---- row validation ----

    @Test
    void importsValidRow() {
        String csv = HEADER + "\n\"What is 2+2?\",\"\",\"EASY\",\"MCQ\",\"1\",\"0\",\"Math\",\"5\","
                + "\"4\",\"\",\"true\",\"3\",\"\",\"false\"";
        BulkUploadResponseDto result = upload(csv);
        assertThat(result.successfulUploads()).isEqualTo(1);
        assertThat(result.failedUploads()).isZero();
    }

    @Test
    void rejectsInvalidDifficulty() {
        String csv = HEADER + "\n\"Q\",\"\",\"EZY\",\"MCQ\",\"1\",\"0\",\"\",\"5\","
                + "\"4\",\"\",\"true\",\"3\",\"\",\"false\"";
        BulkUploadResponseDto result = upload(csv);
        assertThat(result.failedUploads()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.contains("Invalid Difficulty Level"));
    }

    @Test
    void rejectsNonNumericTopicId() {
        String csv = HEADER + "\n\"Q\",\"\",\"EASY\",\"MCQ\",\"1\",\"0\",\"\",\"abc\","
                + "\"4\",\"\",\"true\",\"3\",\"\",\"false\"";
        BulkUploadResponseDto result = upload(csv);
        assertThat(result.failedUploads()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.contains("Topic ID must be a number"));
    }

    @Test
    void rejectsRowWithNoCorrectOption() {
        String csv = HEADER + "\n\"Q\",\"\",\"EASY\",\"MCQ\",\"1\",\"0\",\"\",\"5\","
                + "\"4\",\"\",\"false\",\"3\",\"\",\"false\"";
        BulkUploadResponseDto result = upload(csv);
        assertThat(result.failedUploads()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.contains("must be marked correct"));
    }

    @Test
    void rejectsNonIntegerMarks() {
        String csv = HEADER + "\n\"Q\",\"\",\"EASY\",\"MCQ\",\"0.5\",\"0\",\"\",\"5\","
                + "\"4\",\"\",\"true\",\"3\",\"\",\"false\"";
        BulkUploadResponseDto result = upload(csv);
        assertThat(result.failedUploads()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.contains("Marks must be a whole number"));
    }
}
