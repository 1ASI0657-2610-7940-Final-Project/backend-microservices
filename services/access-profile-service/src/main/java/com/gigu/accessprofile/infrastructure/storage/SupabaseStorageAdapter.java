package com.gigu.accessprofile.infrastructure.storage;

import com.gigu.accessprofile.application.port.out.StoragePort;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SupabaseStorageAdapter implements StoragePort {
    private final String supabaseUrl;
    private final String bucket;

    public SupabaseStorageAdapter(@Value("${SUPABASE_URL:http://localhost}") String supabaseUrl, @Value("${SUPABASE_STORAGE_BUCKET_PORTFOLIO:portfolio}") String bucket) {
        this.supabaseUrl = supabaseUrl;
        this.bucket = bucket;
    }

    public StoredFile store(String userId, String contentType, byte[] bytes) {
        String path = "freelancers/" + userId + "/" + UUID.randomUUID();
        String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
        return new StoredFile(bucket, path, publicUrl, contentType, bytes.length);
    }
}
