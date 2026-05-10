package com.gigu.accessprofile.infrastructure.persistence.repository;

import com.gigu.accessprofile.infrastructure.persistence.entity.RoleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID> { Optional<RoleEntity> findByName(String name); }
