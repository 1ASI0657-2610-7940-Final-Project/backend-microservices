package com.gigu.chatnotification.application.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationCreatedEvent(
    String eventType,
    UUID notificationId,
    UUID recipientId,
    String type,
    String title,
    String message,
    String resourceType,
    UUID resourceId,
    boolean read,
    Instant occurredAt
) {}
