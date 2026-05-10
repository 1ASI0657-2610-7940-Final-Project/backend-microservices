package com.gigu.engagement.interfaces.rest.dto;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record ReviewBody(@NotNull UUID revieweeId, @Min(1) @Max(5) int rating, String comment) {}
