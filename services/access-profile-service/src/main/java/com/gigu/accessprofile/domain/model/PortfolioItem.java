package com.gigu.accessprofile.domain.model;

import java.time.Instant;
import java.util.UUID;

public record PortfolioItem(UUID id, UUID profileId, String title, String description, String bucket, String path, String publicUrl, String contentType, long sizeBytes, Instant createdAt) {}
