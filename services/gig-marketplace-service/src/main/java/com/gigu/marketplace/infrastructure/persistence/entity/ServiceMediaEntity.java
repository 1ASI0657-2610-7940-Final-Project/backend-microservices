package com.gigu.marketplace.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="service_media", schema="marketplace_schema")
public class ServiceMediaEntity {
    @Id public UUID id; @Column(nullable=false) public UUID serviceId; @Column(nullable=false) public String publicUrl; @Column(nullable=false) public String mediaType;
    @Column(nullable=false) public boolean isPrimary; @Column(nullable=false) public String bucket; @Column(nullable=false) public String path;
    @Column(nullable=false) public String contentType; @Column(nullable=false) public long sizeBytes; @Column(nullable=false) public Instant createdAt;
}
