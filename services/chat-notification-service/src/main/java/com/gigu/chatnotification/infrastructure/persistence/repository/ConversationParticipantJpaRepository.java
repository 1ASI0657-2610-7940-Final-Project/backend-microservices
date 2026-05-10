package com.gigu.chatnotification.infrastructure.persistence.repository;
import com.gigu.chatnotification.infrastructure.persistence.entity.ConversationParticipantEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ConversationParticipantJpaRepository extends JpaRepository<ConversationParticipantEntity, UUID> { boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId); }
