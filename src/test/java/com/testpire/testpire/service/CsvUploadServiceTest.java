package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.CreateQuestionRequestDto;
import com.testpire.testpire.dto.response.BulkUploadResponseDto;
import com.testpire.testpire.dto.response.QuestionResponseDto;
import com.testpire.testpire.entity.Institute;
import com.testpire.testpire.entity.Topic;
import com.testpire.testpire.repository.InstituteRepository;
import com.testpire.testpire.repository.TopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CsvUploadServiceTest {

    private static final String HEADER =
            "Question Id,Question Text,Question Image URL,Difficulty Level,Question Type,Marks,Negative Marks,Explanation,Topic ID,Text Format,"
                    + "Option1 Text,Option1 Image URL,Option1 IsCorrect,Option2 Text,Option2 Image URL,Option2 IsCorrect";

    @Mock
    private QuestionService questionService;
    @Mock
    private QuestionImageService questionImageService;
    @Mock
    private InstituteRepository instituteRepository;
    @Mock
    private TopicRepository topicRepository;

    @InjectMocks
    private CsvUploadService service;

    private BulkUploadResponseDto upload(String csv) {
        lenient().when(questionService.createQuestion(any()))
                .thenReturn(QuestionResponseDto.builder().id(1L).build());
        lenient().when(instituteRepository.findById(anyLong()))
                .thenReturn(Optional.of(Institute.builder().id(1L).code("INST").name("Test").build()));
        // Topic ID "5" used by the valid rows resolves to an existing topic in this institute.
        lenient().when(topicRepository.findByIdAndInstituteId(anyLong(), anyLong()))
                .thenReturn(Optional.of(Topic.builder().id(5L).build()));
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
    void rejectsHeaderMissingQuestionId() {
        // First column must now be "Question Id"; a header starting with "Question Text" fails at col 1.
        String[] noId = ("Question Text,Question Image URL,Difficulty Level,Question Type,Marks,Negative Marks,"
                + "Explanation,Topic ID,O1,O1i,O1c,O2,O2i,O2c").split(",");
        assertThatThrownBy(() -> service.validateHeader(noId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("column 1")
                .hasMessageContaining("Question Id");
    }

    @Test
    void rejectsHeaderMissingTopicId() {
        // Topic ID (col 9) replaced by an option column => name mismatch at column 9.
        String[] noTopic = ("Question Id,Question Text,Question Image URL,Difficulty Level,Question Type,Marks,"
                + "Negative Marks,Explanation,Option1 Text,Option1 Image URL,Option1 IsCorrect,Option2 Text").split(",");
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
        String csv = HEADER + "\n\"Q1\",\"What is 2+2?\",\"\",\"EASY\",\"MCQ\",\"1\",\"0\",\"Math\",\"5\","
                + "\"\",\"4\",\"\",\"true\",\"3\",\"\",\"false\"";
        BulkUploadResponseDto result = upload(csv);
        assertThat(result.successfulUploads()).isEqualTo(1);
        assertThat(result.failedUploads()).isZero();
    }

    @Test
    void prefixesInstituteCodeToExternalId() {
        String csv = HEADER + "\n\"Q1\",\"What is 2+2?\",\"\",\"EASY\",\"MCQ\",\"1\",\"0\",\"Math\",\"5\","
                + "\"\",\"4\",\"\",\"true\",\"3\",\"\",\"false\"";
        upload(csv);
        ArgumentCaptor<CreateQuestionRequestDto> captor = ArgumentCaptor.forClass(CreateQuestionRequestDto.class);
        verify(questionService).createQuestion(captor.capture());
        assertThat(captor.getValue().externalId()).isEqualTo("INST_Q1");
    }

    @Test
    void rejectsRowMissingQuestionId() {
        String csv = HEADER + "\n\"\",\"Q\",\"\",\"EASY\",\"MCQ\",\"1\",\"0\",\"\",\"5\","
                + "\"\",\"4\",\"\",\"true\",\"3\",\"\",\"false\"";
        BulkUploadResponseDto result = upload(csv);
        assertThat(result.failedUploads()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.contains("Question Id is required"));
    }

    @Test
    void rejectsDuplicateQuestionIdWithinFile() {
        String row = "\"What is 2+2?\",\"\",\"EASY\",\"MCQ\",\"1\",\"0\",\"\",\"5\","
                + "\"\",\"4\",\"\",\"true\",\"3\",\"\",\"false\"";
        String csv = HEADER + "\n\"Q1\"," + row + "\n\"Q1\"," + row;
        BulkUploadResponseDto result = upload(csv);
        assertThat(result.successfulUploads()).isEqualTo(1);
        assertThat(result.failedUploads()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.contains("Duplicate Question Id"));
    }

    @Test
    void rejectsInvalidDifficulty() {
        String csv = HEADER + "\n\"Q1\",\"Q\",\"\",\"EZY\",\"MCQ\",\"1\",\"0\",\"\",\"5\","
                + "\"\",\"4\",\"\",\"true\",\"3\",\"\",\"false\"";
        BulkUploadResponseDto result = upload(csv);
        assertThat(result.failedUploads()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.contains("Invalid Difficulty Level"));
    }

    @Test
    void rejectsUnknownTopicCode() {
        // A non-numeric Topic ID is treated as a topic code and resolved against the institute;
        // an unknown code (no stub -> empty) is rejected.
        String csv = HEADER + "\n\"Q1\",\"Q\",\"\",\"EASY\",\"MCQ\",\"1\",\"0\",\"\",\"abc\","
                + "\"\",\"4\",\"\",\"true\",\"3\",\"\",\"false\"";
        BulkUploadResponseDto result = upload(csv);
        assertThat(result.failedUploads()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.contains("Topic code \"abc\" does not exist"));
    }

    @Test
    void rejectsRowWithNoCorrectOption() {
        String csv = HEADER + "\n\"Q1\",\"Q\",\"\",\"EASY\",\"MCQ\",\"1\",\"0\",\"\",\"5\","
                + "\"\",\"4\",\"\",\"false\",\"3\",\"\",\"false\"";
        BulkUploadResponseDto result = upload(csv);
        assertThat(result.failedUploads()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.contains("must be marked correct"));
    }

    @Test
    void rejectsNonIntegerMarks() {
        String csv = HEADER + "\n\"Q1\",\"Q\",\"\",\"EASY\",\"MCQ\",\"0.5\",\"0\",\"\",\"5\","
                + "\"\",\"4\",\"\",\"true\",\"3\",\"\",\"false\"";
        BulkUploadResponseDto result = upload(csv);
        assertThat(result.failedUploads()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.contains("Marks must be a whole number"));
    }
}
