package com.gigu.marketplace.interfaces.rest.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
public record UpdateServiceRequest(@DecimalMin(value="0.01") BigDecimal basePrice, @Min(1) Integer deliveryDays, String description) {}
