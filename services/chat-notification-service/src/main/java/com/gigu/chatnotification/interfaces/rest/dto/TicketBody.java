package com.gigu.chatnotification.interfaces.rest.dto;
import jakarta.validation.constraints.*;
public record TicketBody(@NotBlank String subject, @NotBlank String description) {}
