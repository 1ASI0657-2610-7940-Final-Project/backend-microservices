package com.gigu.accessprofile.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "roles", schema = "access_profile_schema")
public class RoleEntity {
    @Id public UUID id;
    @Column(nullable = false, unique = true) public String name;
}
