package com.gigu.marketplace.domain.model;
import java.util.UUID;
public record FreelancerReputation(UUID freelancerId, String displayName, double averageRating, int reviewsCount) {}
