package com.gigu.engagement.infrastructure.security;
import java.util.Set;
import java.util.UUID;
public record AuthUser(UUID id, Set<String> roles) {}
