package com.gigu.marketplace.domain.model;
import java.time.Instant;
import java.util.UUID;
public record ServiceMedia(UUID id, UUID serviceId, String url, String type, boolean primary, String bucket, String objectPath, String contentType, long sizeBytes, int sortOrder, Instant createdAt) {}
