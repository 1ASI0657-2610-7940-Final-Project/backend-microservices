package com.gigu.chatnotification.domain.model;
import java.time.Instant;
import java.util.UUID;
public record Message(UUID id, UUID conversationId, UUID senderId, String content, Instant sentAt) {}
