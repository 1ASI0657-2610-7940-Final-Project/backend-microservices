package com.gigu.marketplace.application.dto;
import java.math.BigDecimal;
public record UpdateServiceCommand(BigDecimal basePrice, Integer deliveryDays, String description, String actorRole, String actorId) {}
