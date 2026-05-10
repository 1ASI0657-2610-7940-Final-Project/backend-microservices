package com.gigu.chatnotification.interfaces.rest.dto;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record ReportBody(@NotNull UUID reportedUserId, @NotBlank String reason, @NotBlank String description) {}
