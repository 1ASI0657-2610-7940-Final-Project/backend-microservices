package com.gigu.accessprofile.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FreelancerProfile(UUID profileId, UUID userId, String displayName, String bio, String academicVerificationStatus, List<String> skills, List<PortfolioItem> portfolioItems, Instant updatedAt) {}
