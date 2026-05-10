package com.gigu.engagement.domain.model;
import com.gigu.engagement.domain.valueobject.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public record Project(UUID id, UUID requestId, UUID agreementId, UUID serviceId, UUID clientId, UUID freelancerId, ProjectStatus status, BigDecimal finalPrice, CurrencyCode currency, Instant createdAt, Instant updatedAt) {}
