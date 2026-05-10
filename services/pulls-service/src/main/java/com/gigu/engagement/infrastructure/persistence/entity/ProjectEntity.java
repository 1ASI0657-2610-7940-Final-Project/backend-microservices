package com.gigu.engagement.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="projects", schema="engagement_schema")
public class ProjectEntity { @Id public UUID id; @Column(nullable=false) public UUID requestId; @Column(nullable=false) public UUID agreementId; @Column(nullable=false) public UUID serviceId; @Column(nullable=false) public UUID clientId; @Column(nullable=false) public UUID freelancerId; @Column(nullable=false) public String status; @Column(nullable=false, precision=12, scale=2) public BigDecimal finalPrice; @Column(nullable=false) public String currency; @Column(nullable=false) public Instant createdAt; @Column(nullable=false) public Instant updatedAt; }
