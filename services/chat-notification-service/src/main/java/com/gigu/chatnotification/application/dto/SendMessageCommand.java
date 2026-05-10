package com.gigu.chatnotification.application.dto;
import java.util.UUID;
public record SendMessageCommand(String content, UUID actorId) {}
