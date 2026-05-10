package com.gigu.marketplace.domain.model;
import com.gigu.marketplace.domain.valueobject.CurrencyCode;
import com.gigu.marketplace.domain.valueobject.ServiceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
public record ServiceOffering(UUID id, UUID freelancerId, String freelancerDisplayName, String title, String description, BigDecimal basePrice, CurrencyCode currency, int deliveryDays, ServiceStatus status, UUID categoryId, String categoryName, List<String> tags, Instant createdAt, Instant updatedAt) {}
