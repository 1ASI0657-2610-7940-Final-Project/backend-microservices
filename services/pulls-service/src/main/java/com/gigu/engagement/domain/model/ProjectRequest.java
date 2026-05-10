package com.gigu.engagement.domain.model;
import com.gigu.engagement.domain.valueobject.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public record ProjectRequest(UUID id, UUID serviceId, UUID clientId, UUID freelancerId, String message, BigDecimal proposedPrice, CurrencyCode currency, int proposedDeliveryDays, ProjectRequestStatus status, Instant createdAt) {}
