package com.testpire.testpire.service;

import com.testpire.testpire.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionImageServiceTest {

    @Mock
    private S3Service s3Service;
    @Mock
    private TopicRepository topicRepository;

    @InjectMocks
    private QuestionImageService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "maxSizeBytes", 1024L);
    }

    @Test
    void toPublicUrlPassesThroughBlank() {
        assertThat(service.toPublicUrl(null)).isNull();
        assertThat(service.toPublicUrl("")).isEmpty();
        verifyNoInteractions(s3Service);
    }

    @Test
    void toPublicUrlPassesThroughAbsoluteUrl() {
        String url = "https://cdn.example.com/a/b.png";
        assertThat(service.toPublicUrl(url)).isEqualTo(url);
        verifyNoInteractions(s3Service);
    }

    @Test
    void toPublicUrlPrefixesBareKey() {
        when(s3Service.buildPublicUrl("inst_1/c/ch/t/x.png")).thenReturn("https://b/inst_1/c/ch/t/x.png");
        assertThat(service.toPublicUrl("inst_1/c/ch/t/x.png")).isEqualTo("https://b/inst_1/c/ch/t/x.png");
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "x.png", "image/png", new byte[0]);
        assertThatThrownBy(() -> service.uploadQuestionImage(1L, 1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
        verifyNoInteractions(topicRepository);
    }

    @Test
    void rejectsDisallowedType() {
        MockMultipartFile file = new MockMultipartFile("file", "x.txt", "text/plain", new byte[]{1, 2, 3});
        assertThatThrownBy(() -> service.uploadQuestionImage(1L, 1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported image type");
        verifyNoInteractions(topicRepository);
    }

    @Test
    void rejectsOversizedFile() {
        byte[] big = new byte[2048];
        MockMultipartFile file = new MockMultipartFile("file", "x.png", "image/png", big);
        assertThatThrownBy(() -> service.uploadQuestionImage(1L, 1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximum size");
        verifyNoInteractions(topicRepository);
    }
}
