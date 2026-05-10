package com.gigu.chatnotification.application.dto;
import java.util.UUID;
public record CreateInternalNotificationCommand(UUID recipientId, String type, String title, String message, String resourceType, UUID resourceId) {}
