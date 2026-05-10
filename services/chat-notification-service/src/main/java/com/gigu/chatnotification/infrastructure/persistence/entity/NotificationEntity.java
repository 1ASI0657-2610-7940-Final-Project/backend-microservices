package com.gigu.chatnotification.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="notifications", schema="chat_notification_schema")
public class NotificationEntity { @Id public UUID id; @Column(nullable=false) public UUID recipientId; @Column(nullable=false) public String type; @Column(nullable=false) public String title; @Column(nullable=false, columnDefinition="TEXT") public String message; public String resourceType; public UUID resourceId; @Column(nullable=false) public boolean read; @Column(nullable=false) public Instant createdAt; public Instant readAt; }
