package com.gigu.chatnotification.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="conversations", schema="chat_notification_schema")
public class ConversationEntity { @Id public UUID id; @Column(nullable=false) public UUID participantA; @Column(nullable=false) public UUID participantB; public UUID projectId; @Column(nullable=false) public Instant createdAt; }
