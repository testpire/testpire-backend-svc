package com.testpire.testpire.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(@Value("${aws.s3.bucket-name}") String bucketName,
                     @Value("${aws.accessKeyId}") String accessKeyId,
                     @Value("${aws.secretKey}") String secretAccessKey,
                     @Value("${aws.region}") String region) {
        this.bucketName = bucketName;
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    public String uploadImage(String base64Image, String folder, String fileName) throws IOException {
        try {
            // Decode base64 image
            String[] imageData = base64Image.split(",");
            String imageType = imageData[0].split(";")[0].split(":")[1];
            byte[] imageBytes = Base64.getDecoder().decode(imageData[1]);

            // Generate unique filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String uniqueFileName = String.format("%s_%s_%s.%s", 
                    fileName, timestamp, UUID.randomUUID().toString().substring(0, 8), 
                    getFileExtension(imageType));

            // Create S3 key
            String s3Key = String.format("%s/%s", folder, uniqueFileName);

            // Upload to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(imageType)
                    .contentLength((long) imageBytes.length)
                    .build();

            PutObjectResponse response = s3Client.putObject(putObjectRequest, 
                    RequestBody.fromInputStream(new ByteArrayInputStream(imageBytes), imageBytes.length));

            log.info("Successfully uploaded image to S3: {}", s3Key);
            
            // Return the S3 URL
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, "ap-south-1", s3Key);

        } catch (Exception e) {
            log.error("Failed to upload image to S3", e);
            throw new IOException("Failed to upload image to S3: " + e.getMessage(), e);
        }
    }

    public String uploadImageFromUrl(String imageUrl, String folder, String fileName) throws IOException {
        try {
            // Download image from URL
            URL url = new URL(imageUrl);
            byte[] imageBytes = url.openStream().readAllBytes();

            // Generate unique filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String uniqueFileName = String.format("%s_%s_%s", 
                    fileName, timestamp, UUID.randomUUID().toString().substring(0, 8));

            // Determine file extension from URL
            String fileExtension = getFileExtensionFromUrl(imageUrl);
            String s3Key = String.format("%s/%s.%s", folder, uniqueFileName, fileExtension);

            // Upload to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentLength((long) imageBytes.length)
                    .build();

            s3Client.putObject(putObjectRequest, 
                    RequestBody.fromInputStream(new ByteArrayInputStream(imageBytes), imageBytes.length));

            log.info("Successfully uploaded image from URL to S3: {}", s3Key);
            
            // Return the S3 URL
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, "ap-south-1", s3Key);

        } catch (Exception e) {
            log.error("Failed to upload image from URL to S3", e);
            throw new IOException("Failed to upload image from URL to S3: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    private String getFileExtensionFromUrl(String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            return fileName.substring(dotIndex + 1);
        }
        return "jpg"; // default extension
    }
}

