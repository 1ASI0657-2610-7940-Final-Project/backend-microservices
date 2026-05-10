package com.gigu.accessprofile.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "portfolio_items", schema = "access_profile_schema")
public class PortfolioItemEntity {
    @Id public UUID id;
    @ManyToOne(optional = false) @JoinColumn(name = "freelancer_profile_id") public FreelancerProfileEntity profile;
    @Column(nullable = false) public String title;
    @Column(columnDefinition = "TEXT") public String description;
    @Column(nullable = false) public String bucket;
    @Column(nullable = false) public String path;
    @Column(nullable = false) public String publicUrl;
    @Column(nullable = false) public String contentType;
    @Column(nullable = false) public long sizeBytes;
    @Column(nullable = false) public Instant createdAt;
}
