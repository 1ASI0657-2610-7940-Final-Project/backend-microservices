package com.gigu.chatnotification.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="messages", schema="chat_notification_schema")
public class MessageEntity { @Id public UUID id; @Column(nullable=false) public UUID conversationId; @Column(nullable=false) public UUID senderId; @Column(nullable=false, columnDefinition="TEXT") public String content; @Column(nullable=false) public Instant sentAt; }
