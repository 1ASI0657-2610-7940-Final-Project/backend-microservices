package com.gigu.chatnotification.infrastructure.persistence.repository;
import com.gigu.chatnotification.infrastructure.persistence.entity.SupportTicketEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SupportTicketJpaRepository extends JpaRepository<SupportTicketEntity, UUID> {}
