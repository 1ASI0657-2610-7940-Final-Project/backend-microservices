package com.gigu.engagement.infrastructure.persistence.repository;
import com.gigu.engagement.infrastructure.persistence.entity.AgreementEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AgreementJpaRepository extends JpaRepository<AgreementEntity, UUID> {}
