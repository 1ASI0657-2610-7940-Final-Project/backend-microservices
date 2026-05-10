package com.gigu.chatnotification.interfaces.rest.dto;
import jakarta.validation.constraints.*;
public record SendMessageBody(@NotBlank @Size(max=2000) String content) {}
