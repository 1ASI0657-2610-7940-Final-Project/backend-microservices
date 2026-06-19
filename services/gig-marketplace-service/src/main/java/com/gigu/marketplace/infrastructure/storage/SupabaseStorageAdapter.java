package com.gigu.marketplace.infrastructure.storage;

import com.gigu.marketplace.application.port.out.StoragePort;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SupabaseStorageAdapter implements StoragePort {
    private static final long MAX_OBJECT_PATH_LENGTH = 512;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Pattern VALID_OBJECT_PATH = Pattern.compile("^services/[A-Za-z0-9-]+/[A-Za-z0-9-]+-[A-Za-z0-9._-]+$");

    private final String url;
    private final String bucket;
    private final SupabaseStorageClient storageClient;

    public SupabaseStorageAdapter(
            @Value("${SUPABASE_URL:http://localhost}") String url,
            @Value("${SUPABASE_STORAGE_BUCKET_GIG_MEDIA:gig-media}") String bucket,
            SupabaseStorageClient storageClient) {
        this.url = requireNonBlank(url, "SUPABASE_URL");
        if (this.url.contains("/storage/v1/s3")) {
            throw new IllegalStateException("SUPABASE_URL must use the standard REST endpoint, not the S3 endpoint");
        }
        this.bucket = requireNonBlank(bucket, "SUPABASE_STORAGE_BUCKET_GIG_MEDIA");
        this.storageClient = storageClient;
    }

    @Override
    public Stored store(String serviceId, String contentType, String originalFileName, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("file is empty");
        }
        String normalizedContentType = normalizeContentType(contentType);
        String objectPath = validateObjectPath(buildObjectPath(serviceId, originalFileName));
        storageClient.upload(bucket, objectPath, normalizedContentType, bytes);
        return new Stored(bucket, objectPath, buildPublicUrl(objectPath), normalizedContentType, bytes.length);
    }

    @Override
    public void delete(String bucket, String objectPath) {
        String validatedBucket = requireNonBlank(bucket, "bucket");
        String validatedObjectPath = validateObjectPath(objectPath);
        storageClient.delete(validatedBucket, validatedObjectPath);
    }

    String buildObjectPath(String serviceId, String originalFileName) {
        return "services/" + sanitizeSegment(serviceId) + "/" + UUID.randomUUID() + "-" + sanitizeFileName(originalFileName);
    }

    String validateObjectPath(String objectPath) {
        if (objectPath == null || objectPath.isBlank()) {
            throw new IllegalArgumentException("objectPath is blank");
        }
        if (objectPath.startsWith("/")) {
            throw new IllegalArgumentException("objectPath must not start with /");
        }
        if (objectPath.contains("\\") || objectPath.contains(" ")) {
            throw new IllegalArgumentException("objectPath contains invalid characters");
        }
        if (objectPath.length() > MAX_OBJECT_PATH_LENGTH) {
            throw new IllegalArgumentException("objectPath is too long");
        }
        if (!StandardCharsets.US_ASCII.newEncoder().canEncode(objectPath)) {
            throw new IllegalArgumentException("objectPath must be ASCII");
        }
        if (!VALID_OBJECT_PATH.matcher(objectPath).matches()) {
            throw new IllegalArgumentException("objectPath must follow services/{serviceId}/{uuid}-{filename}");
        }
        return objectPath;
    }

    String buildPublicUrl(String objectPath) {
        return url + "/storage/v1/object/public/" + bucket + "/" + objectPath;
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("content type is blank");
        }
        String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("unsupported content type");
        }
        return normalized;
    }

    private static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "file";
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String sanitizeSegment(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("serviceId is blank");
        }
        if (value.contains("/") || value.contains("\\") || value.contains(" ")) {
            throw new IllegalArgumentException("serviceId contains invalid characters");
        }
        if (!StandardCharsets.US_ASCII.newEncoder().canEncode(value)) {
            throw new IllegalArgumentException("serviceId must be ASCII");
        }
        return value;
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must not be blank");
        }
        return value.trim();
    }
}
