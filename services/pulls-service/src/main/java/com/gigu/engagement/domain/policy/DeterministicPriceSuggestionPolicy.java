package com.gigu.engagement.domain.policy;
import com.gigu.engagement.domain.model.PriceSuggestion;
import com.gigu.engagement.domain.valueobject.CurrencyCode;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
@Component
public class DeterministicPriceSuggestionPolicy implements PriceSuggestionPolicy {
    public PriceSuggestion suggest(String serviceType, String complexity, String urgency, String freelancerExperience){
        BigDecimal baseMin = switch (serviceType) {
            case "LANDING_PAGE" -> BigDecimal.valueOf(120);
            case "WEB_APP" -> BigDecimal.valueOf(300);
            default -> BigDecimal.valueOf(100);
        };
        BigDecimal baseMax = baseMin.add(BigDecimal.valueOf(100));
        double c = switch (complexity) { case "LOW" -> 0.9; case "HIGH" -> 1.4; default -> 1.1; };
        double u = switch (urgency) { case "URGENT" -> 1.3; case "LOW" -> 0.9; default -> 1.0; };
        double e = switch (freelancerExperience) { case "SENIOR" -> 1.35; case "JUNIOR" -> 0.85; default -> 1.0; };
        BigDecimal factor = BigDecimal.valueOf(c * u * e);
        return new PriceSuggestion(baseMin.multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP), baseMax.multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP), CurrencyCode.PEN, serviceType, complexity, urgency, freelancerExperience);
    }
}
