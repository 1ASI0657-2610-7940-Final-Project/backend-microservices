package com.gigu.chatnotification.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="conversation_participants", schema="chat_notification_schema")
public class ConversationParticipantEntity { @Id @GeneratedValue(strategy = GenerationType.UUID) public UUID id; @Column(nullable=false) public UUID conversationId; @Column(nullable=false) public UUID userId; }
