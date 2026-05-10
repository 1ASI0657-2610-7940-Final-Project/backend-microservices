package com.gigu.marketplace.interfaces.rest.dto;
import com.gigu.marketplace.domain.valueobject.CurrencyCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public record CreateServiceRequest(@NotBlank String title, @NotBlank String description, @NotNull @DecimalMin(value="0.01") BigDecimal basePrice, @NotNull CurrencyCode currency, @NotNull UUID categoryId, @Min(1) int deliveryDays, List<String> tags) {}
