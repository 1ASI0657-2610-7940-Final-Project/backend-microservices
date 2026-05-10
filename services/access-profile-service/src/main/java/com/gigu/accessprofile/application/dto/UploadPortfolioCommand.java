package com.gigu.accessprofile.application.dto;

public record UploadPortfolioCommand(String title, String description, String contentType, long sizeBytes, byte[] bytes) {}
