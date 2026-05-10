package com.gigu.accessprofile.infrastructure.persistence.repository;

import com.gigu.accessprofile.infrastructure.persistence.entity.FreelancerProfileEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreelancerProfileJpaRepository extends JpaRepository<FreelancerProfileEntity, UUID> { Optional<FreelancerProfileEntity> findByUser_Id(UUID userId); }
