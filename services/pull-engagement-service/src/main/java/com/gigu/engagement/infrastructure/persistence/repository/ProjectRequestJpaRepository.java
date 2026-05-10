package com.gigu.engagement.infrastructure.persistence.repository;
import com.gigu.engagement.infrastructure.persistence.entity.ProjectRequestEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProjectRequestJpaRepository extends JpaRepository<ProjectRequestEntity, UUID> { List<ProjectRequestEntity> findByFreelancerId(UUID freelancerId); List<ProjectRequestEntity> findByClientId(UUID clientId); }
