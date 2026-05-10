package com.gigu.chatnotification.domain.model;
import java.util.UUID;
public record ConversationParticipant(UUID conversationId, UUID userId) {}
