package com.gigu.accessprofile.infrastructure.storage;

import com.gigu.accessprofile.application.port.out.StoragePort;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SupabaseStorageAdapter implements StoragePort {
    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final String bucket;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public SupabaseStorageAdapter(@Value("${SUPABASE_URL:http://localhost}") String supabaseUrl, @Value("${SUPABASE_SERVICE_ROLE_KEY:}") String serviceRoleKey, @Value("${SUPABASE_STORAGE_BUCKET_PORTFOLIO:portfolio}") String bucket) {
        this.supabaseUrl = supabaseUrl;
        this.serviceRoleKey = serviceRoleKey;
        this.bucket = bucket;
    }

    public StoredFile store(String userId, String contentType, String originalFileName, byte[] bytes) {
        String safeFileName = sanitizeFileName(originalFileName);
        String path = "freelancers/" + userId + "/" + UUID.randomUUID() + "-" + safeFileName;
        String objectUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + path;
        HttpRequest request = HttpRequest.newBuilder(URI.create(objectUrl))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("supabase storage upload failed with status " + response.statusCode());
            }
        } catch (Exception e) {
            throw new IllegalStateException("supabase storage upload failed", e);
        }
        String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
        return new StoredFile(bucket, path, publicUrl, contentType, bytes.length);
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return "file";
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
