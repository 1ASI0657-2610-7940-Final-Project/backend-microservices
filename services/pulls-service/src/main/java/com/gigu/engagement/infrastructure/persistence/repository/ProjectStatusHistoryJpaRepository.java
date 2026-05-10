package com.gigu.engagement.infrastructure.persistence.repository;
import com.gigu.engagement.infrastructure.persistence.entity.ProjectStatusHistoryEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProjectStatusHistoryJpaRepository extends JpaRepository<ProjectStatusHistoryEntity, UUID> { List<ProjectStatusHistoryEntity> findByProjectIdOrderByChangedAtAsc(UUID projectId); }
