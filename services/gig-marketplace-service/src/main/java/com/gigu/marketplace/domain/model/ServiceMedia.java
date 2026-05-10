package com.gigu.marketplace.domain.model;
import java.time.Instant;
import java.util.UUID;
public record ServiceMedia(UUID id, UUID serviceId, String url, String type, boolean primary, String bucket, String path, String contentType, long sizeBytes, Instant createdAt) {}
