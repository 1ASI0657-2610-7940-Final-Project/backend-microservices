package com.gigu.engagement.application.dto;
import java.util.UUID;
public record CreateReviewCommand(UUID revieweeId, int rating, String comment, String actorId) {}
