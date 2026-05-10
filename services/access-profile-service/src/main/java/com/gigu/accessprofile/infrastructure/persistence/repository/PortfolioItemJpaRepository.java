package com.gigu.accessprofile.infrastructure.persistence.repository;

import com.gigu.accessprofile.infrastructure.persistence.entity.PortfolioItemEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioItemJpaRepository extends JpaRepository<PortfolioItemEntity, UUID> {
    List<PortfolioItemEntity> findByProfileId(UUID profileId);
    Optional<PortfolioItemEntity> findByIdAndProfile_User_Id(UUID id, UUID userId);
}
