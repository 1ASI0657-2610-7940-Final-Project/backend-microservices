package com.gigu.engagement.interfaces.rest.dto;
import com.gigu.engagement.domain.valueobject.CurrencyCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;
public record CreateRequestBody(@NotNull UUID serviceId,@NotNull UUID freelancerId,String message,@NotNull @DecimalMin(value="0.01") BigDecimal proposedPrice,@NotNull CurrencyCode currency,@Min(1) int proposedDeliveryDays) {}
