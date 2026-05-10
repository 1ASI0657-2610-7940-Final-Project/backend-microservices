package com.gigu.engagement.domain.model;
import com.gigu.engagement.domain.valueobject.ProjectStatus;
import java.time.Instant;
import java.util.UUID;
public record ProjectStatusHistory(UUID id, UUID projectId, ProjectStatus status, String comment, Instant changedAt) {}
