package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.CreateMaterialUploadUrlRequestDto;
import com.testpire.testpire.dto.request.CreateTopicMaterialRequestDto;
import com.testpire.testpire.dto.request.UpdateTopicMaterialRequestDto;
import com.testpire.testpire.dto.response.MaterialUploadUrlResponseDto;
import com.testpire.testpire.dto.response.TopicMaterialResponseDto;
import com.testpire.testpire.entity.Chapter;
import com.testpire.testpire.entity.Subject;
import com.testpire.testpire.entity.Topic;
import com.testpire.testpire.entity.TopicMaterial;
import com.testpire.testpire.enums.MaterialType;
import com.testpire.testpire.enums.TextFormat;
import com.testpire.testpire.repository.TopicMaterialRepository;
import com.testpire.testpire.repository.TopicRepository;
import com.testpire.testpire.util.RequestUtils;
import com.testpire.testpire.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Teaching materials attached to a topic. File-backed materials (PDF/PPT/VIDEO) move via presigned
 * S3 URLs — the bytes never pass through this service; NOTE materials store inline text and LINK
 * materials store an external URL. Every query is scoped to the caller's institute for multi-tenancy
 * (mirroring {@code TopicService}); SUPER_ADMIN (null institute) is unscoped.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TopicMaterialService {

    /** contentType -> file extension, for the S3 object key. Also the upload allowlist. */
    private static final Map<String, String> ALLOWED_FILE_TYPES = Map.of(
            "application/pdf", "pdf",
            "application/vnd.ms-powerpoint", "ppt",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx",
            "video/mp4", "mp4",
            "video/webm", "webm",
            "video/quicktime", "mov");

    /** Which content types each file-backed material type accepts (prevents mislabeling). */
    private static final Map<MaterialType, Set<String>> TYPE_CONTENT_TYPES = Map.of(
            MaterialType.PDF, Set.of("application/pdf"),
            MaterialType.PPT, Set.of(
                    "application/vnd.ms-powerpoint",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
            MaterialType.VIDEO, Set.of("video/mp4", "video/webm", "video/quicktime"));

    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(15);
    private static final Duration DOWNLOAD_URL_TTL = Duration.ofMinutes(15);

    private final TopicMaterialRepository materialRepository;
    private final TopicRepository topicRepository;
    private final S3Service s3Service;

    @Value("${app.materials.max-size-bytes:52428800}") // 50 MB
    private long maxSizeBytes;

    /**
     * Step 1 of a file upload: validate the declared content type/size and return a presigned PUT URL
     * plus the {@code s3Key} the client must echo back on register. Verifies the topic belongs to the
     * caller's institute before minting anything.
     */
    @Transactional(readOnly = true)
    public MaterialUploadUrlResponseDto createUploadUrl(Long topicId, CreateMaterialUploadUrlRequestDto request) {
        Topic topic = loadTopicScopedWithHierarchy(topicId);

        String extension = ALLOWED_FILE_TYPES.get(request.contentType());
        if (extension == null) {
            throw new IllegalArgumentException(
                    "Unsupported content type: " + request.contentType() + ". Allowed: " + ALLOWED_FILE_TYPES.keySet());
        }
        if (request.sizeBytes() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    "File exceeds the maximum size of " + (maxSizeBytes / (1024 * 1024)) + " MB");
        }

        String key = String.format("%s/materials/%s.%s", folderFor(topic), UUID.randomUUID(), extension);
        var url = s3Service.generatePresignedPutUrl(key, request.contentType(), UPLOAD_URL_TTL);

        log.info("Minted upload URL for topic {} (institute {}): {}", topicId, topic.getInstituteId(), key);
        return new MaterialUploadUrlResponseDto(
                url.toString(), key, request.contentType(), UPLOAD_URL_TTL.toSeconds());
    }

    /**
     * Step 2 (file) / single step (NOTE, LINK): register the material. For file-backed types the S3
     * object is verified to exist and its actual size re-checked against the limit (the presigned PUT
     * limit was advisory). The recorded content type/size come from S3, not the client's claim.
     */
    @Transactional
    public TopicMaterialResponseDto createMaterial(Long topicId, CreateTopicMaterialRequestDto request) {
        Topic topic = loadTopicScopedWithHierarchy(topicId);

        TopicMaterial.TopicMaterialBuilder builder = TopicMaterial.builder()
                .topicId(topic.getId())
                .instituteId(topic.getInstituteId())
                .type(request.type())
                .title(request.title())
                .description(request.description())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .createdBy(RequestUtils.getCurrentUsername());

        switch (request.type()) {
            case PDF, PPT, VIDEO -> applyFilePayload(request, builder);
            case NOTE -> {
                if (request.content() == null || request.content().isBlank()) {
                    throw new IllegalArgumentException("content is required for a NOTE material");
                }
                builder.content(request.content())
                        .contentFormat(request.contentFormat() != null ? request.contentFormat() : TextFormat.PLAIN);
            }
            case LINK -> {
                if (request.externalUrl() == null || request.externalUrl().isBlank()) {
                    throw new IllegalArgumentException("externalUrl is required for a LINK material");
                }
                builder.externalUrl(request.externalUrl());
            }
        }

        TopicMaterial saved = materialRepository.save(builder.build());
        log.info("Created {} material {} on topic {}", saved.getType(), saved.getId(), topicId);
        return TopicMaterialResponseDto.fromEntity(saved);
    }

    private void applyFilePayload(CreateTopicMaterialRequestDto request, TopicMaterial.TopicMaterialBuilder builder) {
        if (request.s3Key() == null || request.s3Key().isBlank()) {
            throw new IllegalArgumentException("s3Key is required for a " + request.type() + " material");
        }
        HeadObjectResponse head = s3Service.headObject(request.s3Key())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No uploaded object found at s3Key " + request.s3Key() + " — upload the file first"));

        long actualSize = head.contentLength() != null ? head.contentLength() : 0L;
        if (actualSize > maxSizeBytes) {
            s3Service.deleteObject(request.s3Key());
            throw new IllegalArgumentException(
                    "Uploaded file exceeds the maximum size of " + (maxSizeBytes / (1024 * 1024)) + " MB");
        }

        String actualContentType = head.contentType() != null ? head.contentType() : request.contentType();
        Set<String> allowed = TYPE_CONTENT_TYPES.get(request.type());
        if (actualContentType == null || allowed == null || !allowed.contains(actualContentType)) {
            s3Service.deleteObject(request.s3Key());
            throw new IllegalArgumentException(
                    "Content type " + actualContentType + " is not valid for a " + request.type() + " material");
        }

        builder.s3Key(request.s3Key())
                .fileName(request.fileName())
                .contentType(actualContentType)
                .sizeBytes(actualSize);
    }

    @Transactional(readOnly = true)
    public List<TopicMaterialResponseDto> listMaterials(Long topicId) {
        loadTopicScoped(topicId); // tenancy + existence
        Long instituteId = RequestUtils.getCurrentUserInstituteId();
        List<TopicMaterial> materials = (instituteId != null
                ? materialRepository.findByTopicIdAndInstituteIdOrderBySortOrderAscIdAsc(topicId, instituteId)
                : materialRepository.findByTopicIdOrderBySortOrderAscIdAsc(topicId));
        return materials.stream().map(TopicMaterialResponseDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public TopicMaterialResponseDto getMaterial(Long id) {
        return TopicMaterialResponseDto.fromEntity(findMaterialScoped(id));
    }

    /**
     * Resolve a downloadable URL for a material: a short-lived presigned GET URL for file-backed
     * materials, or the external URL for a LINK. NOTE materials have no download (read {@code content}).
     */
    @Transactional(readOnly = true)
    public String getDownloadUrl(Long id) {
        TopicMaterial material = findMaterialScoped(id);
        return switch (material.getType()) {
            case PDF, PPT, VIDEO -> s3Service.generatePresignedGetUrl(material.getS3Key(), DOWNLOAD_URL_TTL).toString();
            case LINK -> material.getExternalUrl();
            case NOTE -> throw new IllegalArgumentException("A NOTE material has no downloadable file");
        };
    }

    @Transactional
    public TopicMaterialResponseDto updateMaterial(Long id, UpdateTopicMaterialRequestDto request) {
        TopicMaterial material = findMaterialScoped(id);

        Optional.ofNullable(request.title()).ifPresent(material::setTitle);
        Optional.ofNullable(request.description()).ifPresent(material::setDescription);
        Optional.ofNullable(request.sortOrder()).ifPresent(material::setSortOrder);
        if (material.getType() == MaterialType.NOTE) {
            Optional.ofNullable(request.content()).ifPresent(material::setContent);
            Optional.ofNullable(request.contentFormat()).ifPresent(material::setContentFormat);
        }
        if (material.getType() == MaterialType.LINK) {
            Optional.ofNullable(request.externalUrl()).ifPresent(material::setExternalUrl);
        }
        material.setUpdatedBy(RequestUtils.getCurrentUsername());

        TopicMaterial saved = materialRepository.save(material);
        return TopicMaterialResponseDto.fromEntity(saved);
    }

    @Transactional
    public void deleteMaterial(Long id) {
        TopicMaterial material = findMaterialScoped(id);
        if (material.getType().isFileBacked() && material.getS3Key() != null) {
            // FK cascade only reaches DB rows; the S3 object must be removed explicitly.
            s3Service.deleteObject(material.getS3Key());
        }
        materialRepository.delete(material);
        log.info("Deleted material {} from topic {}", id, material.getTopicId());
    }

    // --- helpers ---------------------------------------------------------------

    /** Load a topic scoped to the caller's institute (with hierarchy for the S3 folder). */
    private Topic loadTopicScopedWithHierarchy(Long topicId) {
        Topic topic = topicRepository.findByIdWithHierarchy(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found with ID: " + topicId));
        Long instituteId = RequestUtils.getCurrentUserInstituteId();
        if (instituteId != null && !instituteId.equals(topic.getInstituteId())) {
            throw new IllegalArgumentException("Topic not found with ID: " + topicId);
        }
        return topic;
    }

    private Topic loadTopicScoped(Long topicId) {
        Long instituteId = RequestUtils.getCurrentUserInstituteId();
        return (instituteId != null
                ? topicRepository.findByIdAndInstituteId(topicId, instituteId)
                : topicRepository.findById(topicId))
                .orElseThrow(() -> new IllegalArgumentException("Topic not found with ID: " + topicId));
    }

    private TopicMaterial findMaterialScoped(Long id) {
        Long instituteId = RequestUtils.getCurrentUserInstituteId();
        return (instituteId != null
                ? materialRepository.findByIdAndInstituteId(id, instituteId)
                : materialRepository.findById(id))
                .orElseThrow(() -> new IllegalArgumentException("Material not found with ID: " + id));
    }

    /** {@code institute_<id>/<subject-slug>/<chapter-slug>/<topic-slug>} from the topic hierarchy. */
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
