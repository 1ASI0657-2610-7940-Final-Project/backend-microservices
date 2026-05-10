package com.gigu.marketplace.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="freelancer_reputation", schema="marketplace_schema")
public class FreelancerReputationEntity { @Id public UUID freelancerId; @Column(nullable=false) public String displayName; @Column(nullable=false) public double averageRating; @Column(nullable=false) public int reviewsCount; }
