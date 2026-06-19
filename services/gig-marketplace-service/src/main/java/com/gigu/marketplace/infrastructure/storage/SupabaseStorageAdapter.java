package com.gigu.marketplace.infrastructure.storage;

import com.gigu.marketplace.application.exception.SupabaseStorageException;
import com.gigu.marketplace.application.port.out.StoragePort;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SupabaseStorageAdapter implements StoragePort {
    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageAdapter.class);
    private static final long MAX_OBJECT_PATH_LENGTH = 512;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Pattern VALID_OBJECT_PATH = Pattern.compile("^services/[A-Za-z0-9-]+/[A-Za-z0-9-]+\\.(jpg|png|webp)$");

    private final String url;
    private final String serviceRoleKey;
    private final String bucket;
    private final HttpClient httpClient;

    @Autowired
    public SupabaseStorageAdapter(
            @Value("${SUPABASE_URL}") String url,
            @Value("${SUPABASE_SERVICE_ROLE_KEY}") String serviceRoleKey,
            @Value("${SUPABASE_STORAGE_BUCKET_GIG_MEDIA}") String bucket) {
        this(url, serviceRoleKey, bucket, HttpClient.newHttpClient());
    }

    SupabaseStorageAdapter(String url, String serviceRoleKey, String bucket, HttpClient httpClient) {
        this.url = requireNonBlank(url, "SUPABASE_URL");
        this.serviceRoleKey = requireNonBlank(serviceRoleKey, "SUPABASE_SERVICE_ROLE_KEY");
        this.bucket = requireNonBlank(bucket, "SUPABASE_STORAGE_BUCKET_GIG_MEDIA");
        if (this.url.contains("/storage/v1/s3")) {
            throw new IllegalStateException("SUPABASE_URL must use the standard REST endpoint, not the S3 endpoint");
        }
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    @Override
    public Stored store(String serviceId, String contentType, String originalFileName, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("file is empty");
        }
        String normalizedContentType = normalizeContentType(contentType);
        String objectPath = validateObjectPath(buildObjectPath(serviceId, normalizedContentType));
        String uploadUrl = buildObjectUrl(objectPath);

        HttpRequest request = HttpRequest.newBuilder(URI.create(uploadUrl))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .header("x-upsert", "false")
                .header("Content-Type", normalizedContentType)
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (!isSuccessful(response.statusCode())) {
                logFailure("upload", uploadUrl, objectPath, normalizedContentType, bytes.length, response.statusCode(), response.body());
                throw new SupabaseStorageException("Supabase storage upload failed: " + sanitizeBody(response.body()));
            }
        } catch (SupabaseStorageException e) {
            throw e;
        } catch (Exception e) {
            throw new SupabaseStorageException("Supabase storage upload failed: " + sanitizeBody(e.getMessage()));
        }

        return new Stored(bucket, objectPath, buildPublicUrl(objectPath), normalizedContentType, bytes.length);
    }

    @Override
    public void delete(String bucket, String objectPath) {
        String validatedBucket = requireNonBlank(bucket, "bucket");
        String validatedObjectPath = validateObjectPath(objectPath);
        String uploadUrl = buildObjectUrl(validatedObjectPath, validatedBucket);

        HttpRequest request = HttpRequest.newBuilder(URI.create(uploadUrl))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .DELETE()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (!isSuccessful(response.statusCode())) {
                logFailure("delete", uploadUrl, validatedObjectPath, null, 0L, response.statusCode(), response.body());
                throw new SupabaseStorageException("Supabase storage delete failed: " + sanitizeBody(response.body()));
            }
        } catch (SupabaseStorageException e) {
            throw e;
        } catch (Exception e) {
            throw new SupabaseStorageException("Supabase storage delete failed: " + sanitizeBody(e.getMessage()));
        }
    }

    String buildObjectPath(String serviceId, String contentType) {
        String extension = extensionFor(contentType);
        return "services/" + sanitizeSegment(serviceId) + "/" + UUID.randomUUID() + "." + extension;
    }

    String buildObjectUrl(String objectPath) {
        return buildObjectUrl(objectPath, bucket);
    }

    String buildObjectUrl(String objectPath, String bucketName) {
        return url + "/storage/v1/object/" + bucketName + "/" + objectPath;
    }

    String buildPublicUrl(String objectPath) {
        return url + "/storage/v1/object/public/" + bucket + "/" + objectPath;
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
            throw new IllegalArgumentException("objectPath must follow services/{serviceId}/{uuid}.{extension}");
        }
        return objectPath;
    }

    private String extensionFor(String contentType) {
        String normalized = normalizeContentType(contentType);
        return switch (normalized) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new IllegalArgumentException("unsupported content type");
        };
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

    private static String sanitizeBody(String body) {
        if (body == null || body.isBlank()) {
            return "(empty response body)";
        }
        return body.replaceAll("\\s+", " ").trim();
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must not be blank");
        }
        return value.trim();
    }

    private static boolean isSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private void logFailure(String operation, String uploadUrl, String objectPath, String contentType, long sizeBytes, int statusCode, String responseBody) {
        log.error(
                "Supabase storage {} failed. status={} bucket={} objectPath={} contentType={} sizeBytes={} uploadUrl={} responseBody={}",
                operation,
                statusCode,
                bucket,
                objectPath,
                contentType,
                sizeBytes,
                uploadUrl,
                sanitizeBody(responseBody));
    }
}
