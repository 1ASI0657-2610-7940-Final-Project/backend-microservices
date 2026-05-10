package com.gigu.chatnotification.domain.model;
import java.time.Instant;
import java.util.UUID;
public record SupportTicket(UUID id, UUID userId, String subject, String description, String status, Instant createdAt) {}
