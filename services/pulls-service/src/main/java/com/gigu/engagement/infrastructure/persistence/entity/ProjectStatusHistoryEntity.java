package com.gigu.engagement.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="project_status_history", schema="engagement_schema")
public class ProjectStatusHistoryEntity { @Id public UUID id; @Column(nullable=false) public UUID projectId; @Column(nullable=false) public String status; @Column(columnDefinition="TEXT") public String comment; @Column(nullable=false) public Instant changedAt; }
