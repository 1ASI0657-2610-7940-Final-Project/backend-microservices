package com.gigu.marketplace.infrastructure.security;
import java.util.Set;
import java.util.UUID;
public record AuthUser(UUID id, Set<String> roles) {}
