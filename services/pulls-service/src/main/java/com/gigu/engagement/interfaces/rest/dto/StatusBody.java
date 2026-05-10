package com.gigu.engagement.interfaces.rest.dto;
import com.gigu.engagement.domain.valueobject.ProjectStatus;
public record StatusBody(ProjectStatus status, String comment) {}
