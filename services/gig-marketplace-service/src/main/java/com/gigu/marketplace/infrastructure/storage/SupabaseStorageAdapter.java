package com.gigu.marketplace.infrastructure.storage;
import com.gigu.marketplace.application.port.out.StoragePort;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class SupabaseStorageAdapter implements StoragePort {
    private final String url; private final String bucket;
    public SupabaseStorageAdapter(@Value("${SUPABASE_URL:http://localhost}") String url, @Value("${SUPABASE_STORAGE_BUCKET_GIG_MEDIA:gig-media}") String bucket){this.url=url;this.bucket=bucket;}
    public Stored store(String serviceId, String contentType, byte[] bytes){ String path="services/"+serviceId+"/"+UUID.randomUUID(); return new Stored(bucket,path,url+"/storage/v1/object/public/"+bucket+"/"+path,contentType,bytes.length); }
}
