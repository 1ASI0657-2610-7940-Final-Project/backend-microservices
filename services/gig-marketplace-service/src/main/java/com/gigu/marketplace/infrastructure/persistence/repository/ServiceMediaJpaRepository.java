package com.gigu.marketplace.infrastructure.persistence.repository;
import com.gigu.marketplace.infrastructure.persistence.entity.ServiceMediaEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ServiceMediaJpaRepository extends JpaRepository<ServiceMediaEntity, UUID> { List<ServiceMediaEntity> findByServiceId(UUID serviceId); }
