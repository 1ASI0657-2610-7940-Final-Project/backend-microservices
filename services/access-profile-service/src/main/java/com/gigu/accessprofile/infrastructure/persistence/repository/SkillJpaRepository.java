package com.gigu.accessprofile.infrastructure.persistence.repository;

import com.gigu.accessprofile.infrastructure.persistence.entity.SkillEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillJpaRepository extends JpaRepository<SkillEntity, UUID> { Optional<SkillEntity> findByNameIgnoreCase(String name); }
