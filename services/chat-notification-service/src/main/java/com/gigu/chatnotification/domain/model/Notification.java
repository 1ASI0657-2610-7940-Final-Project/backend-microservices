package com.gigu.chatnotification.domain.model;
import java.time.Instant;
import java.util.UUID;
public record Notification(UUID id, UUID recipientId, String type, String title, String message, String resourceType, UUID resourceId, boolean read, Instant createdAt, Instant readAt) {}
