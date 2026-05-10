package com.gigu.engagement.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="agreements", schema="engagement_schema")
public class AgreementEntity { @Id public UUID id; @Column(nullable=false) public UUID requestId; @Column(nullable=false, precision=12, scale=2) public BigDecimal finalPrice; @Column(nullable=false) public String currency; @Column(nullable=false) public int finalDeliveryDays; @Column(columnDefinition="TEXT") public String responseMessage; @Column(nullable=false) public Instant createdAt; }
