package com.gigu.engagement.domain.model;
import com.gigu.engagement.domain.valueobject.CurrencyCode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public record Agreement(UUID id, UUID requestId, BigDecimal finalPrice, CurrencyCode currency, int finalDeliveryDays, String responseMessage, Instant createdAt) {}
