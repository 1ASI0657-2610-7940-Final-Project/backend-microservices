package com.gigu.marketplace.application.dto;
import com.gigu.marketplace.domain.valueobject.CurrencyCode;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public record CreateServiceCommand(String title, String description, BigDecimal basePrice, CurrencyCode currency, UUID categoryId, int deliveryDays, List<String> tags, UUID freelancerId, String freelancerDisplayName, boolean freelancerRole) {}
