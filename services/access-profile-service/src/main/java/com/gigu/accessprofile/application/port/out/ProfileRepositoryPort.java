package com.gigu.accessprofile.application.port.out;

import com.gigu.accessprofile.domain.model.FreelancerProfile;
import com.gigu.accessprofile.domain.model.PortfolioItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepositoryPort {
    FreelancerProfile createEmpty(UUID userId, String displayName);
    Optional<FreelancerProfile> findByUserId(UUID userId);
    FreelancerProfile update(UUID userId, String bio, List<String> skills);
    PortfolioItem addPortfolioItem(UUID userId, String title, String description, String bucket, String path, String publicUrl, String contentType, long sizeBytes);
    void deletePortfolioItem(UUID userId, UUID itemId);
}
