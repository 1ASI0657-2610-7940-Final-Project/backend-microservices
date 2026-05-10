package com.gigu.engagement.domain.model;
import java.time.Instant;
import java.util.UUID;
public record Review(UUID id, UUID projectId, UUID reviewerId, UUID revieweeId, int rating, String comment, Instant createdAt) {}
