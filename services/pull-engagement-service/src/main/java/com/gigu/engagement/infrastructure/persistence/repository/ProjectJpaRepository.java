package com.gigu.engagement.infrastructure.persistence.repository;
import com.gigu.engagement.infrastructure.persistence.entity.ProjectEntity;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, UUID> {
    @Query("select p from ProjectEntity p where p.clientId = :userId or p.freelancerId = :userId")
    List<ProjectEntity> findByParticipant(UUID userId);
}
