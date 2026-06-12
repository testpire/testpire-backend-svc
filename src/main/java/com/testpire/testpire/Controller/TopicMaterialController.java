package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequirePermission;
import com.testpire.testpire.dto.request.CreateMaterialUploadUrlRequestDto;
import com.testpire.testpire.dto.request.CreateTopicMaterialRequestDto;
import com.testpire.testpire.dto.request.UpdateTopicMaterialRequestDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.enums.Permission;
import com.testpire.testpire.service.TopicMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Teaching materials (PPT/PDF/video/inline note/external link) for a topic.
 *
 * <p>File uploads are two-step: {@code POST .../materials/upload-url} returns a presigned S3 PUT URL
 * (the client uploads bytes straight to S3), then {@code POST .../materials} registers the row.
 * NOTE/LINK materials skip the upload and POST directly. Downloads go through
 * {@code GET .../materials/{id}/download-url}, which returns a fresh short-lived presigned GET URL.</p>
 */
@RestController
@RequestMapping("/api/topics/{topicId}/materials")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Topic Materials", description = "Teaching resources (ppt/pdf/video/note/link) attached to a topic")
@SecurityRequirement(name = "Bearer Authentication")
public class TopicMaterialController {

    private final TopicMaterialService materialService;

    @PostMapping("/upload-url")
    @RequirePermission(Permission.TOPIC_MATERIAL_CREATE)
    @Operation(summary = "Get a presigned URL to upload a material file",
            description = "Returns a short-lived presigned S3 PUT URL for a PDF/PPT/VIDEO. Upload the bytes to it, then register the material via POST /materials with the returned s3Key.")
    public ResponseEntity<ApiResponseDto> createUploadUrl(
            @Parameter(description = "Topic ID", required = true, example = "1") @PathVariable Long topicId,
            @Valid @RequestBody CreateMaterialUploadUrlRequestDto request) {
        try {
            var response = materialService.createUploadUrl(topicId, request);
            return ResponseEntity.ok(ApiResponseDto.success("Upload URL generated", response));
        } catch (Exception e) {
            log.error("Error generating upload URL for topic {}", topicId, e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to generate upload URL: " + e.getMessage()));
        }
    }

    @PostMapping
    @RequirePermission(Permission.TOPIC_MATERIAL_CREATE)
    @Operation(summary = "Register a material on a topic",
            description = "Creates a material. For PDF/PPT/VIDEO pass the s3Key from a prior upload-url call; for NOTE pass content; for LINK pass externalUrl.")
    public ResponseEntity<ApiResponseDto> createMaterial(
            @Parameter(description = "Topic ID", required = true, example = "1") @PathVariable Long topicId,
            @Valid @RequestBody CreateTopicMaterialRequestDto request) {
        try {
            var material = materialService.createMaterial(topicId, request);
            return ResponseEntity.ok(ApiResponseDto.success("Material created successfully", material));
        } catch (Exception e) {
            log.error("Error creating material on topic {}", topicId, e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to create material: " + e.getMessage()));
        }
    }

    @GetMapping
    @RequirePermission(Permission.TOPIC_MATERIAL_READ)
    @Operation(summary = "List a topic's materials", description = "Returns the topic's materials ordered by sortOrder.")
    public ResponseEntity<ApiResponseDto> listMaterials(
            @Parameter(description = "Topic ID", required = true, example = "1") @PathVariable Long topicId) {
        try {
            var materials = materialService.listMaterials(topicId);
            return ResponseEntity.ok(ApiResponseDto.success("Materials retrieved successfully", materials));
        } catch (Exception e) {
            log.error("Error listing materials for topic {}", topicId, e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to list materials: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequirePermission(Permission.TOPIC_MATERIAL_READ)
    @Operation(summary = "Get a single material's metadata")
    public ResponseEntity<ApiResponseDto> getMaterial(
            @Parameter(description = "Topic ID", required = true, example = "1") @PathVariable Long topicId,
            @Parameter(description = "Material ID", required = true, example = "1") @PathVariable Long id) {
        try {
            var material = materialService.getMaterial(id);
            return ResponseEntity.ok(ApiResponseDto.success("Material retrieved successfully", material));
        } catch (Exception e) {
            log.error("Error getting material {}", id, e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get material: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/download-url")
    @RequirePermission(Permission.TOPIC_MATERIAL_READ)
    @Operation(summary = "Get a download URL for a material",
            description = "Returns a short-lived presigned GET URL for a file material, or the external URL for a LINK.")
    public ResponseEntity<ApiResponseDto> getDownloadUrl(
            @Parameter(description = "Topic ID", required = true, example = "1") @PathVariable Long topicId,
            @Parameter(description = "Material ID", required = true, example = "1") @PathVariable Long id) {
        try {
            String url = materialService.getDownloadUrl(id);
            return ResponseEntity.ok(ApiResponseDto.success("Download URL generated", Map.of("downloadUrl", url)));
        } catch (Exception e) {
            log.error("Error generating download URL for material {}", id, e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to generate download URL: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequirePermission(Permission.TOPIC_MATERIAL_UPDATE)
    @Operation(summary = "Update a material's metadata (and NOTE/LINK payload)")
    public ResponseEntity<ApiResponseDto> updateMaterial(
            @Parameter(description = "Topic ID", required = true, example = "1") @PathVariable Long topicId,
            @Parameter(description = "Material ID", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateTopicMaterialRequestDto request) {
        try {
            var material = materialService.updateMaterial(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Material updated successfully", material));
        } catch (Exception e) {
            log.error("Error updating material {}", id, e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to update material: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequirePermission(Permission.TOPIC_MATERIAL_DELETE)
    @Operation(summary = "Delete a material", description = "Removes the row and, for file materials, the underlying S3 object.")
    public ResponseEntity<ApiResponseDto> deleteMaterial(
            @Parameter(description = "Topic ID", required = true, example = "1") @PathVariable Long topicId,
            @Parameter(description = "Material ID", required = true, example = "1") @PathVariable Long id) {
        try {
            materialService.deleteMaterial(id);
            return ResponseEntity.ok(ApiResponseDto.success("Material deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting material {}", id, e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to delete material: " + e.getMessage()));
        }
    }
}
