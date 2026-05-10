package com.gigu.chatnotification.infrastructure.persistence.repository;
import com.gigu.chatnotification.infrastructure.persistence.entity.MessageEntity;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MessageJpaRepository extends JpaRepository<MessageEntity, UUID> { Page<MessageEntity> findByConversationIdOrderBySentAtDesc(UUID conversationId, Pageable pageable); }
