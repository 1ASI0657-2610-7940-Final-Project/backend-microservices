package com.gigu.engagement.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="project_requests", schema="engagement_schema")
public class ProjectRequestEntity { @Id public UUID id; @Column(nullable=false) public UUID serviceId; @Column(nullable=false) public UUID clientId; @Column(nullable=false) public UUID freelancerId; @Column(nullable=false, columnDefinition="TEXT") public String message; @Column(nullable=false, precision=12, scale=2) public BigDecimal proposedPrice; @Column(nullable=false) public String currency; @Column(nullable=false) public int proposedDeliveryDays; @Column(nullable=false) public String status; @Column(nullable=false) public Instant createdAt; }
