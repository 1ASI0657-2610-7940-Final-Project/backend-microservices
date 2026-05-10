package com.gigu.engagement.application.dto;
import com.gigu.engagement.domain.valueobject.ProjectRequestStatus;
import java.math.BigDecimal;
public record DecisionCommand(ProjectRequestStatus decision, BigDecimal finalPrice, Integer finalDeliveryDays, String responseMessage, String actorRole, String actorId) {}
