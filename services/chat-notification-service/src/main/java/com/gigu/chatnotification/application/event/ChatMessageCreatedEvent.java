package com.gigu.chatnotification.application.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatMessageCreatedEvent(
    String eventType,
    UUID messageId,
    UUID conversationId,
    UUID senderId,
    UUID receiverId,
    String content,
    String contentPreview,
    Instant occurredAt,
    Map<String, String> metadata
) {}
