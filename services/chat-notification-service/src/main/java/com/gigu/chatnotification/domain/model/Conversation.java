package com.gigu.chatnotification.domain.model;
import java.time.Instant;
import java.util.UUID;
public record Conversation(UUID id, UUID participantA, UUID participantB, UUID projectId, Instant createdAt) {}
