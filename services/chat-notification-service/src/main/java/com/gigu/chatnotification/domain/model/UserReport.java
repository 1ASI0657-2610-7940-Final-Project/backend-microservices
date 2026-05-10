package com.gigu.chatnotification.domain.model;
import java.time.Instant;
import java.util.UUID;
public record UserReport(UUID id, UUID reporterId, UUID reportedUserId, String reason, String description, String status, Instant createdAt) {}
