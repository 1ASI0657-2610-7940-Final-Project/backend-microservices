package com.gigu.chatnotification.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="user_reports", schema="chat_notification_schema")
public class UserReportEntity { @Id public UUID id; @Column(nullable=false) public UUID reporterId; @Column(nullable=false) public UUID reportedUserId; @Column(nullable=false) public String reason; @Column(nullable=false, columnDefinition="TEXT") public String description; @Column(nullable=false) public String status; @Column(nullable=false) public Instant createdAt; }
