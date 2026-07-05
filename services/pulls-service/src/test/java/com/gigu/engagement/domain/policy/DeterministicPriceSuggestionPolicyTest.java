package com.gigu.engagement.domain.policy;

import com.gigu.engagement.domain.model.PriceSuggestion;
import com.gigu.engagement.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de dominio para la política determinística de sugerencia de precios.
 * No requiere contexto de Spring: la policy es lógica pura del dominio de contratación.
 */
class DeterministicPriceSuggestionPolicyTest {

    private final DeterministicPriceSuggestionPolicy policy = new DeterministicPriceSuggestionPolicy();

    @Test
    @DisplayName("Con factores neutros el precio es exactamente la tarifa base del tipo de servicio (x1.1)")
    void neutralFactorsYieldBaseTierPrice() {
        // Complejidad no-LOW/HIGH -> 1.1 ; urgencia no-URGENT/LOW -> 1.0 ; experiencia no-SENIOR/JUNIOR -> 1.0
        PriceSuggestion webApp = policy.suggest("WEB_APP", "MEDIUM", "NORMAL", "MID");
        PriceSuggestion landing = policy.suggest("LANDING_PAGE", "MEDIUM", "NORMAL", "MID");
        PriceSuggestion other = policy.suggest("CONSULTING", "MEDIUM", "NORMAL", "MID");

        assertAll(
            () -> assertEquals(0, new BigDecimal("330.00").compareTo(webApp.suggestedMinPrice())),
            () -> assertEquals(0, new BigDecimal("440.00").compareTo(webApp.suggestedMaxPrice())),
            () -> assertEquals(0, new BigDecimal("132.00").compareTo(landing.suggestedMinPrice())),
            () -> assertEquals(0, new BigDecimal("242.00").compareTo(landing.suggestedMaxPrice())),
            // Tipo de servicio desconocido cae en la tarifa base por defecto (100 / 200)
            () -> assertEquals(0, new BigDecimal("110.00").compareTo(other.suggestedMinPrice())),
            () -> assertEquals(0, new BigDecimal("220.00").compareTo(other.suggestedMaxPrice()))
        );
    }

    @Test
    @DisplayName("El tipo de servicio determina el escalón base: WEB_APP > LANDING_PAGE > desconocido")
    void serviceTypeDeterminesBaseTier() {
        BigDecimal webApp = policy.suggest("WEB_APP", "MEDIUM", "NORMAL", "MID").suggestedMinPrice();
        BigDecimal landing = policy.suggest("LANDING_PAGE", "MEDIUM", "NORMAL", "MID").suggestedMinPrice();
        BigDecimal unknown = policy.suggest("SOMETHING_ELSE", "MEDIUM", "NORMAL", "MID").suggestedMinPrice();

        assertAll(
            () -> assertTrue(webApp.compareTo(landing) > 0, "WEB_APP debe costar más que LANDING_PAGE"),
            () -> assertTrue(landing.compareTo(unknown) > 0, "LANDING_PAGE debe costar más que el tipo por defecto")
        );
    }

    @Test
    @DisplayName("Mayor complejidad encarece el precio: HIGH > MEDIUM > LOW")
    void higherComplexityIncreasesPrice() {
        BigDecimal low = policy.suggest("WEB_APP", "LOW", "NORMAL", "MID").suggestedMinPrice();
        BigDecimal medium = policy.suggest("WEB_APP", "MEDIUM", "NORMAL", "MID").suggestedMinPrice();
        BigDecimal high = policy.suggest("WEB_APP", "HIGH", "NORMAL", "MID").suggestedMinPrice();

        assertAll(
            () -> assertTrue(high.compareTo(medium) > 0, "HIGH debe superar a MEDIUM"),
            () -> assertTrue(medium.compareTo(low) > 0, "MEDIUM debe superar a LOW")
        );
    }

    @Test
    @DisplayName("Mayor urgencia encarece el precio: URGENT > NORMAL > LOW")
    void higherUrgencyIncreasesPrice() {
        BigDecimal low = policy.suggest("WEB_APP", "MEDIUM", "LOW", "MID").suggestedMinPrice();
        BigDecimal normal = policy.suggest("WEB_APP", "MEDIUM", "NORMAL", "MID").suggestedMinPrice();
        BigDecimal urgent = policy.suggest("WEB_APP", "MEDIUM", "URGENT", "MID").suggestedMinPrice();

        assertAll(
            () -> assertTrue(urgent.compareTo(normal) > 0, "URGENT debe superar a NORMAL"),
            () -> assertTrue(normal.compareTo(low) > 0, "NORMAL debe superar a LOW")
        );
    }

    @Test
    @DisplayName("Mayor experiencia del freelancer encarece el precio: SENIOR > MID > JUNIOR")
    void seniorExperienceCostsMoreThanJunior() {
        BigDecimal junior = policy.suggest("WEB_APP", "MEDIUM", "NORMAL", "JUNIOR").suggestedMinPrice();
        BigDecimal mid = policy.suggest("WEB_APP", "MEDIUM", "NORMAL", "MID").suggestedMinPrice();
        BigDecimal senior = policy.suggest("WEB_APP", "MEDIUM", "NORMAL", "SENIOR").suggestedMinPrice();

        assertAll(
            () -> assertTrue(senior.compareTo(mid) > 0, "SENIOR debe superar a MID"),
            () -> assertTrue(mid.compareTo(junior) > 0, "MID debe superar a JUNIOR")
        );
    }

    @Test
    @DisplayName("Invariantes: máximo > mínimo, moneda PEN y la sugerencia refleja los parámetros de entrada")
    void suggestionInvariantsAndEcho() {
        PriceSuggestion s = policy.suggest("WEB_APP", "HIGH", "URGENT", "SENIOR");

        assertAll(
            () -> assertTrue(s.suggestedMaxPrice().compareTo(s.suggestedMinPrice()) > 0, "el máximo debe superar al mínimo"),
            () -> assertTrue(s.suggestedMinPrice().compareTo(BigDecimal.ZERO) > 0, "el mínimo debe ser positivo"),
            () -> assertEquals(CurrencyCode.PEN, s.currency()),
            () -> assertEquals("WEB_APP", s.serviceType()),
            () -> assertEquals("HIGH", s.complexity()),
            () -> assertEquals("URGENT", s.urgency()),
            () -> assertEquals("SENIOR", s.freelancerExperience()),
            // El precio se redondea a 2 decimales (escala monetaria)
            () -> assertEquals(2, s.suggestedMinPrice().scale()),
            () -> assertEquals(2, s.suggestedMaxPrice().scale())
        );
    }
}
