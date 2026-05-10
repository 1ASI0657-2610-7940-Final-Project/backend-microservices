package com.gigu.accessprofile.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "freelancer_profiles", schema = "access_profile_schema")
public class FreelancerProfileEntity {
    @Id public UUID id;
    @OneToOne(optional = false) @JoinColumn(name = "user_id") public UserEntity user;
    @Column(nullable = false) public String displayName;
    @Column(columnDefinition = "TEXT") public String bio;
    @Column(nullable = false) public String academicVerificationStatus;
    @Column(nullable = false) public Instant updatedAt;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(schema = "access_profile_schema", name = "freelancer_skills", joinColumns = @JoinColumn(name = "freelancer_profile_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    public Set<SkillEntity> skills = new HashSet<>();
}
