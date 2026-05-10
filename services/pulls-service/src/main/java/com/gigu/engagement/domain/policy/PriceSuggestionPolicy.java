package com.gigu.engagement.domain.policy;
import com.gigu.engagement.domain.model.PriceSuggestion;
public interface PriceSuggestionPolicy { PriceSuggestion suggest(String serviceType, String complexity, String urgency, String freelancerExperience); }
