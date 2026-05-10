package com.gigu.chatnotification.application.dto;
import java.util.UUID;
public record CreateTicketCommand(String subject, String description, UUID userId) {}
