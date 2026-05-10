package com.gigu.accessprofile.domain.model;

import com.gigu.accessprofile.domain.valueobject.RoleName;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record User(UUID id, String firstName, String lastName, String email, String passwordHash, Set<RoleName> roles, Instant createdAt) {}
