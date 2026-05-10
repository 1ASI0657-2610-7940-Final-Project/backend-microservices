package com.gigu.marketplace.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="service_offerings", schema="marketplace_schema")
public class ServiceOfferingEntity {
    @Id public UUID id; @Column(nullable=false) public UUID freelancerId; @Column(nullable=false) public String freelancerDisplayName;
    @Column(nullable=false) public String title; @Column(nullable=false, columnDefinition="TEXT") public String description;
    @Column(nullable=false, precision=12, scale=2) public java.math.BigDecimal basePrice; @Column(nullable=false) public String currency;
    @Column(nullable=false) public int deliveryDays; @Column(nullable=false) public String status;
    @ManyToOne(optional=false) @JoinColumn(name="category_id") public ServiceCategoryEntity category;
    @Column(nullable=false) public String tags;
    @Column(nullable=false) public Instant createdAt; @Column(nullable=false) public Instant updatedAt;
}
