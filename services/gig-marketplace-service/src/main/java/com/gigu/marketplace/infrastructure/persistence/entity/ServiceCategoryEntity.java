package com.gigu.marketplace.infrastructure.persistence.entity;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="service_categories", schema="marketplace_schema")
public class ServiceCategoryEntity { @Id public UUID id; @Column(nullable=false, unique=true) public String name; }
