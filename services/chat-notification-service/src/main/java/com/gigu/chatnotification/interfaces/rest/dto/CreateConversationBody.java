package com.gigu.chatnotification.interfaces.rest.dto;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record CreateConversationBody(@NotNull UUID participantId, UUID projectId) {}
