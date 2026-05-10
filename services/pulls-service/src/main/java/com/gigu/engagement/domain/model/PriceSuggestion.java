package com.gigu.engagement.domain.model;
import com.gigu.engagement.domain.valueobject.CurrencyCode;
import java.math.BigDecimal;
public record PriceSuggestion(BigDecimal suggestedMinPrice, BigDecimal suggestedMaxPrice, CurrencyCode currency, String serviceType, String complexity, String urgency, String freelancerExperience) {}
