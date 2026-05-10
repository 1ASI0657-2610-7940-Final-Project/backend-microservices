package com.gigu.accessprofile.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "access_profile_schema")
public class UserEntity {
    @Id public UUID id;
    @Column(nullable = false) public String firstName;
    @Column(nullable = false) public String lastName;
    @Column(nullable = false, unique = true) public String email;
    @Column(nullable = false) public String passwordHash;
    @Column(nullable = false) public Instant createdAt;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(schema = "access_profile_schema", name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    public Set<RoleEntity> roles = new HashSet<>();
}
