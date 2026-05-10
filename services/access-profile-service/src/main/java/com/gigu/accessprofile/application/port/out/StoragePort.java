package com.gigu.accessprofile.application.port.out;

public interface StoragePort {
    StoredFile store(String userId, String contentType, byte[] bytes);
    record StoredFile(String bucket, String path, String publicUrl, String contentType, long sizeBytes) {}
}
