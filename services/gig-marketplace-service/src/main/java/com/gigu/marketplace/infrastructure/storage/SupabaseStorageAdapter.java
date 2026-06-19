package com.gigu.marketplace.infrastructure.storage;
import com.gigu.marketplace.application.port.out.StoragePort;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class SupabaseStorageAdapter implements StoragePort {
    private final String url; private final String serviceRoleKey; private final String bucket;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    public SupabaseStorageAdapter(@Value("${SUPABASE_URL}") String url, @Value("${SUPABASE_SERVICE_ROLE_KEY}") String serviceRoleKey, @Value("${SUPABASE_STORAGE_BUCKET_GIG_MEDIA}") String bucket){this.url=url;this.serviceRoleKey=serviceRoleKey;this.bucket=bucket;}
    public Stored store(String serviceId, String contentType, String originalFileName, byte[] bytes){
        String extension = extensionFor(contentType, originalFileName);
        String objectPath = "services/" + serviceId + "/" + UUID.randomUUID() + "." + extension;
        HttpRequest request = HttpRequest.newBuilder(URI.create(url + "/storage/v1/object/" + bucket + "/" + encodePath(objectPath)))
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
        return new Stored(bucket, objectPath, url + "/storage/v1/object/public/" + bucket + "/" + encodePath(objectPath), contentType, bytes.length);
    }
    public void delete(String bucket, String objectPath) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url + "/storage/v1/object/" + bucket + "/" + encodePath(objectPath)))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("supabase storage delete failed with status " + response.statusCode());
            }
        } catch (Exception e) {
            throw new IllegalStateException("supabase storage delete failed", e);
        }
    }
    private String extensionFor(String contentType, String originalFileName) {
        if (contentType != null) {
            String normalized = contentType.toLowerCase(Locale.ROOT);
            if (normalized.equals("image/png")) return "png";
            if (normalized.equals("image/webp")) return "webp";
        }
        if (originalFileName != null) {
            String lower = originalFileName.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".jpeg") || lower.endsWith(".jpg")) return "jpg";
            if (lower.endsWith(".png")) return "png";
            if (lower.endsWith(".webp")) return "webp";
        }
        return "jpg";
    }
    private String encodePath(String path) {
        return String.join("/", java.util.Arrays.stream(path.split("/")).map(part -> URLEncoder.encode(part, StandardCharsets.UTF_8)).toList());
    }
}
