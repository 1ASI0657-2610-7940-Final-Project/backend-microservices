package com.gigu.marketplace.infrastructure.persistence.repository;
import com.gigu.marketplace.infrastructure.persistence.entity.FreelancerReputationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FreelancerReputationJpaRepository extends JpaRepository<FreelancerReputationEntity, UUID> {}
