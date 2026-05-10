package com.gigu.marketplace.infrastructure.storage;
import com.gigu.marketplace.application.port.out.StoragePort;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class SupabaseStorageAdapter implements StoragePort {
    private final String url; private final String serviceRoleKey; private final String bucket;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    public SupabaseStorageAdapter(@Value("${SUPABASE_URL:http://localhost}") String url, @Value("${SUPABASE_SERVICE_ROLE_KEY:}") String serviceRoleKey, @Value("${SUPABASE_STORAGE_BUCKET_GIG_MEDIA:gig-media}") String bucket){this.url=url;this.serviceRoleKey=serviceRoleKey;this.bucket=bucket;}
    public Stored store(String serviceId, String contentType, String originalFileName, byte[] bytes){
        String safeFileName = sanitizeFileName(originalFileName);
        String path="services/"+serviceId+"/"+UUID.randomUUID()+"-"+safeFileName;
        HttpRequest request = HttpRequest.newBuilder(URI.create(url+"/storage/v1/object/"+bucket+"/"+path))
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
        return new Stored(bucket,path,url+"/storage/v1/object/public/"+bucket+"/"+path,contentType,bytes.length);
    }
    private String sanitizeFileName(String fileName){ if(fileName==null||fileName.isBlank()) return "file"; return fileName.replaceAll("[^a-zA-Z0-9._-]","_"); }
}
