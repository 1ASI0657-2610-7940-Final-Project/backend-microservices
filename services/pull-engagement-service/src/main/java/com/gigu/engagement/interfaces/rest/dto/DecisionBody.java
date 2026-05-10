package com.gigu.engagement.interfaces.rest.dto;
import com.gigu.engagement.domain.valueobject.ProjectRequestStatus;
import java.math.BigDecimal;
public record DecisionBody(ProjectRequestStatus decision, BigDecimal finalPrice, Integer finalDeliveryDays, String responseMessage) {}
