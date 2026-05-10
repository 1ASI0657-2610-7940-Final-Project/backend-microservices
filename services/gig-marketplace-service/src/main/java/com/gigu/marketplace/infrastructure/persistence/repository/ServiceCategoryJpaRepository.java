package com.gigu.marketplace.infrastructure.persistence.repository;
import com.gigu.marketplace.infrastructure.persistence.entity.ServiceCategoryEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ServiceCategoryJpaRepository extends JpaRepository<ServiceCategoryEntity, UUID> {
    Optional<ServiceCategoryEntity> findByNameIgnoreCase(String name);
}
