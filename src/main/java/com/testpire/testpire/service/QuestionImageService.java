package com.testpire.testpire.service;

import com.testpire.testpire.entity.Chapter;
import com.testpire.testpire.entity.Subject;
import com.testpire.testpire.entity.Topic;
import com.testpire.testpire.repository.TopicRepository;
import com.testpire.testpire.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Owns all question/option image handling: builds the per-institute S3 folder hierarchy,
 * validates uploads, and translates between the stored S3 key and the public URL returned
 * to clients. The DB persists the key; {@link #toPublicUrl(String)} renders it for responses.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionImageService {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif");

    private final S3Service s3Service;
    private final TopicRepository topicRepository;

    @Value("${app.images.max-size-bytes:2097152}")
    private long maxSizeBytes;

    /**
     * Upload a question image for {@code topicId}. Validates the topic belongs to {@code instituteId}
     * and that the file is an allowed image within the size limit. Returns the stored S3 key.
     */
    public String uploadQuestionImage(Long topicId, Long instituteId, MultipartFile file) throws IOException {
        validate(file);
        return store(file, buildFolder(topicId, instituteId));
    }

    /** Upload an option image under the question's topic folder (in an {@code options/} subfolder). */
    public String uploadOptionImage(Long topicId, Long instituteId, MultipartFile file) throws IOException {
        validate(file);
        return store(file, buildFolder(topicId, instituteId) + "/options");
    }

    /**
     * Re-host an external image URL under the topic's hierarchy. Used by the CSV bulk path, which has
     * no authenticated institute context, so tenancy is taken from the topic itself. Returns the key.
     */
    public String uploadFromUrl(Long topicId, String imageUrl, boolean isOption) throws IOException {
        String folder = buildFolder(topicId, null) + (isOption ? "/options" : "");
        return s3Service.uploadImageFromUrl(imageUrl, folder, UUID.randomUUID().toString());
    }

    /**
     * Render a stored value as a public URL. Blank passes through; an already-absolute URL (legacy
     * full-URL rows) passes through unchanged; a bare key is prefixed with the configured base URL.
     */
    public String toPublicUrl(String stored) {
        if (stored == null || stored.isBlank()) {
            return stored;
        }
        if (stored.startsWith("http://") || stored.startsWith("https://")) {
            return stored;
        }
        return s3Service.buildPublicUrl(stored);
    }

    private String store(MultipartFile file, String folder) throws IOException {
        String extension = ALLOWED_TYPES.get(file.getContentType());
        String key = String.format("%s/%s.%s", folder, UUID.randomUUID(), extension);
        return s3Service.putObject(file.getBytes(), key, file.getContentType());
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    "Image exceeds the maximum size of " + (maxSizeBytes / 1024) + " KB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.containsKey(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported image type: " + contentType + ". Allowed: " + ALLOWED_TYPES.keySet());
        }
    }

    /** Load the topic with its hierarchy, verify tenancy, and build its slugified folder path. */
    private String buildFolder(Long topicId, Long instituteId) {
        Topic topic = topicRepository.findByIdWithHierarchy(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found with ID: " + topicId));
        if (instituteId != null && !instituteId.equals(topic.getInstituteId())) {
            throw new IllegalArgumentException("Topic does not belong to the specified institute");
        }
        return folderFor(topic);
    }

    /** {@code institute_<id>/<subject-slug>/<chapter-slug>/<topic-slug>} from the hierarchy. */
    private String folderFor(Topic topic) {
        Chapter chapter = topic.getChapter();
        Subject subject = chapter.getSubject();
        return String.format("institute_%d/%s/%s/%s",
                topic.getInstituteId(),
                SlugUtils.slugify(subject.getName(), subject.getId()),
                SlugUtils.slugify(chapter.getName(), chapter.getId()),
                SlugUtils.slugify(topic.getName(), topic.getId()));
    }
}
