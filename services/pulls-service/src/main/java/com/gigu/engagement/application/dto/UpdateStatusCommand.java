package com.gigu.engagement.application.dto;
import com.gigu.engagement.domain.valueobject.ProjectStatus;
public record UpdateStatusCommand(ProjectStatus status, String comment, String actorRole, String actorId) {}
