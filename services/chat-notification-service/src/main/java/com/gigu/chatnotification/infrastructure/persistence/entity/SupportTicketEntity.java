package com.gigu.chatnotification.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="support_tickets", schema="chat_notification_schema")
public class SupportTicketEntity { @Id public UUID id; @Column(nullable=false) public UUID userId; @Column(nullable=false) public String subject; @Column(nullable=false, columnDefinition="TEXT") public String description; @Column(nullable=false) public String status; @Column(nullable=false) public Instant createdAt; }
