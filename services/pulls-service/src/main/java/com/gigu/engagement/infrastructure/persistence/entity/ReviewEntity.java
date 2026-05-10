package com.gigu.engagement.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="reviews", schema="engagement_schema")
public class ReviewEntity { @Id public UUID id; @Column(nullable=false) public UUID projectId; @Column(nullable=false) public UUID reviewerId; @Column(nullable=false) public UUID revieweeId; @Column(nullable=false) public int rating; @Column(columnDefinition="TEXT") public String comment; @Column(nullable=false) public Instant createdAt; }
