package com.gigu.chatnotification.interfaces.rest.dto;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record InternalNotificationBody(@NotNull UUID recipientId, @NotBlank String type, @NotBlank String title, @NotBlank String message, String resourceType, UUID resourceId) {}
