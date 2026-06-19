package com.gigu.marketplace.infrastructure.storage;

import com.gigu.marketplace.application.exception.SupabaseStorageException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class SupabaseStorageClient {
    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageClient.class);
    private static final String CIRCUIT_BREAKER_NAME = "supabaseStorage";

    private final String url;
    private final String serviceRoleKey;
    private final HttpClient httpClient;

    public SupabaseStorageClient(
            @Value("${SUPABASE_URL:http://localhost}") String url,
            @Value("${SUPABASE_SERVICE_ROLE_KEY:}") String serviceRoleKey) {
        this.url = requireNonBlank(url, "SUPABASE_URL");
        this.serviceRoleKey = requireNonBlank(serviceRoleKey, "SUPABASE_SERVICE_ROLE_KEY");
        if (this.url.contains("/storage/v1/s3")) {
            throw new IllegalStateException("SUPABASE_URL must use the standard REST endpoint, not the S3 endpoint");
        }
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "uploadFallback")
    public void upload(String bucket, String objectPath, String contentType, byte[] bytes) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(uploadUrl(bucket, objectPath)))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .header("x-upsert", "false")
                .header("Content-Type", contentType)
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (!isSuccessful(response.statusCode())) {
                logFailure("upload", bucket, objectPath, contentType, bytes.length, response.statusCode(), response.body());
                throw new RestClientException("supabase storage upload failed with status " + response.statusCode() + ": " + sanitizeBody(response.body()));
            }
        } catch (IOException e) {
            throw new RestClientException("supabase storage upload failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RestClientException("supabase storage upload interrupted", e);
        }
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "deleteFallback")
    public void delete(String bucket, String objectPath) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(uploadUrl(bucket, objectPath)))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .timeout(Duration.ofSeconds(5))
                .DELETE()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (!isSuccessful(response.statusCode())) {
                logFailure("delete", bucket, objectPath, null, 0L, response.statusCode(), response.body());
                throw new RestClientException("supabase storage delete failed with status " + response.statusCode() + ": " + sanitizeBody(response.body()));
            }
        } catch (IOException e) {
            throw new RestClientException("supabase storage delete failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RestClientException("supabase storage delete interrupted", e);
        }
    }

    void uploadFallback(String bucket, String objectPath, String contentType, byte[] bytes, Throwable throwable) {
        logFallback("upload", bucket, objectPath, contentType, bytes == null ? 0L : bytes.length, throwable);
        throw new SupabaseStorageException("Supabase storage upload failed: " + safeCause(throwable), throwable);
    }

    void deleteFallback(String bucket, String objectPath, Throwable throwable) {
        logFallback("delete", bucket, objectPath, null, 0L, throwable);
        throw new SupabaseStorageException("Supabase storage delete failed: " + safeCause(throwable), throwable);
    }

    private String uploadUrl(String bucket, String objectPath) {
        return url + "/storage/v1/object/" + bucket + "/" + objectPath;
    }

    private void logFailure(String operation, String bucket, String objectPath, String contentType, long sizeBytes, int statusCode, String responseBody) {
        log.error(
                "Supabase storage {} failed status={} bucket={} objectPath={} contentType={} sizeBytes={} uploadUrl={} responseBody={}",
                operation,
                statusCode,
                bucket,
                objectPath,
                contentType,
                sizeBytes,
                sanitizeBody(uploadUrl(bucket, objectPath)),
                sanitizeBody(responseBody));
    }

    private void logFallback(String operation, String bucket, String objectPath, String contentType, long sizeBytes, Throwable throwable) {
        log.warn(
                "circuit breaker fallback triggered target=Supabase Storage operation={} bucket={} objectPath={} contentType={} sizeBytes={} cause={}",
                operation,
                bucket,
                objectPath,
                contentType,
                sizeBytes,
                safeCause(throwable));
    }

    private static boolean isSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private static String sanitizeBody(String body) {
        if (body == null || body.isBlank()) {
            return "(empty response body)";
        }
        return body.replaceAll("\\s+", " ").trim();
    }

    private static String safeCause(Throwable throwable) {
        if (throwable == null) {
            return "unknown";
        }
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        return throwable.getClass().getSimpleName() + ": " + message.replaceAll("\\s+", " ").trim();
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must not be blank");
        }
        return value.trim();
    }
}
