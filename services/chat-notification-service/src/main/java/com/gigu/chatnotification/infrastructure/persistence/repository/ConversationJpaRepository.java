package com.gigu.chatnotification.infrastructure.persistence.repository;
import com.gigu.chatnotification.infrastructure.persistence.entity.ConversationEntity;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface ConversationJpaRepository extends JpaRepository<ConversationEntity, UUID> {
    Optional<ConversationEntity> findByParticipantAAndParticipantBAndProjectId(UUID participantA, UUID participantB, UUID projectId);
    @Query("select c from ConversationEntity c where c.participantA=:userId or c.participantB=:userId order by c.createdAt desc")
    List<ConversationEntity> listForUser(UUID userId);
}
