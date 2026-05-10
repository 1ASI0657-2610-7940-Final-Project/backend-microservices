package com.gigu.engagement.infrastructure.persistence.repository;
import com.gigu.engagement.infrastructure.persistence.entity.ReviewEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, UUID> { boolean existsByProjectIdAndReviewerId(UUID projectId, UUID reviewerId); List<ReviewEntity> findByProjectId(UUID projectId); }
