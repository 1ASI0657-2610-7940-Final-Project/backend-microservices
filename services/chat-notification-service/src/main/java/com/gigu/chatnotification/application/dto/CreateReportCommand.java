package com.gigu.chatnotification.application.dto;
import java.util.UUID;
public record CreateReportCommand(UUID reportedUserId, String reason, String description, UUID reporterId) {}
