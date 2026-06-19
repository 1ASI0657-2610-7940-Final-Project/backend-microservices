package com.gigu.marketplace.application.port.out;
public interface StoragePort {
    record Stored(String bucket, String objectPath, String publicUrl, String contentType, long sizeBytes) {}
    Stored store(String serviceId, String contentType, String originalFileName, byte[] bytes);
    void delete(String bucket, String objectPath);
}
