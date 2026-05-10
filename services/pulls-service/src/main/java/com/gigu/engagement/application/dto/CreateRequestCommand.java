package com.gigu.engagement.application.dto;
import com.gigu.engagement.domain.valueobject.CurrencyCode;
import java.math.BigDecimal;
import java.util.UUID;
public record CreateRequestCommand(UUID serviceId, UUID freelancerId, String message, BigDecimal proposedPrice, CurrencyCode currency, int proposedDeliveryDays, UUID clientId, boolean clientRole) {}
