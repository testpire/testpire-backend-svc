package com.testpire.testpire.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Base64;

/**
 * Low-level S3 access: writes bytes under a caller-supplied key and returns that key
 * (never a full URL). URL construction lives in {@link #buildPublicUrl(String)} so the
 * stored value stays portable across bucket/region/CDN changes.
 */
@Service
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;
    private final String region;
    private final String publicBaseUrl;

    public S3Service(@Value("${aws.s3.bucket-name}") String bucketName,
                     @Value("${aws.region}") String region,
                     @Value("${aws.s3.public-base-url:}") String publicBaseUrl,
                     AwsCredentialsProvider awsCredentialsProvider) {
        this.bucketName = bucketName;
        this.region = region;
        this.publicBaseUrl = publicBaseUrl;

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    /** Single upload primitive. Writes {@code bytes} at {@code key} and returns the key. */
    public String putObject(byte[] bytes, String key, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength((long) bytes.length)
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(new ByteArrayInputStream(bytes), bytes.length));

        log.info("Uploaded object to S3: {}", key);
        return key;
    }

    /**
     * Decode a data-URI / base64 string and store it under {@code folder/<fileName>.<ext>}.
     * Returns the S3 key.
     */
    public String uploadImage(String base64Image, String folder, String fileName) throws IOException {
        try {
            String[] imageData = base64Image.split(",");
            String imageType = imageData[0].split(";")[0].split(":")[1];
            byte[] imageBytes = Base64.getDecoder().decode(imageData[1]);

            String key = String.format("%s/%s.%s", folder, fileName, getFileExtension(imageType));
            return putObject(imageBytes, key, imageType);
        } catch (Exception e) {
            log.error("Failed to upload image to S3", e);
            throw new IOException("Failed to upload image to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Download an image from an external URL and store it under {@code folder/<fileName>.<ext>}.
     * Returns the S3 key. The host is validated to block SSRF against internal addresses.
     */
    public String uploadImageFromUrl(String imageUrl, String folder, String fileName) throws IOException {
        try {
            validateExternalUrl(imageUrl);

            URL url = URI.create(imageUrl).toURL();
            byte[] imageBytes = url.openStream().readAllBytes();

            String fileExtension = getFileExtensionFromUrl(imageUrl);
            String key = String.format("%s/%s.%s", folder, fileName, fileExtension);

            // contentType is best-effort here; the extension drives client rendering.
            return putObject(imageBytes, key, "application/octet-stream");
        } catch (IOException e) {
            log.error("Failed to upload image from URL to S3", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to upload image from URL to S3", e);
            throw new IOException("Failed to upload image from URL to S3: " + e.getMessage(), e);
        }
    }

    /** Build the public URL for a stored key. Honors {@code aws.s3.public-base-url} when set. */
    public String buildPublicUrl(String key) {
        String base = (publicBaseUrl != null && !publicBaseUrl.isBlank())
                ? publicBaseUrl.replaceAll("/+$", "")
                : String.format("https://%s.s3.%s.amazonaws.com", bucketName, region);
        return base + "/" + key;
    }

    /** Reject non-http(s) schemes and hosts that resolve to loopback/link-local/private ranges. */
    private void validateExternalUrl(String imageUrl) throws IOException {
        URI uri = URI.create(imageUrl);
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IOException("Unsupported image URL scheme: " + scheme);
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IOException("Image URL has no host");
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isLoopbackAddress() || address.isAnyLocalAddress()
                        || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                        || address.isMulticastAddress()) {
                    throw new IOException("Image URL resolves to a disallowed address: " + host);
                }
            }
        } catch (UnknownHostException e) {
            throw new IOException("Cannot resolve image URL host: " + host, e);
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
        String path = URI.create(url).getPath();
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "jpg"; // default extension
    }
}
