package com.gigu.chatnotification.application.dto;
import java.util.UUID;
public record CreateConversationCommand(UUID participantId, UUID projectId, UUID actorId) {}
