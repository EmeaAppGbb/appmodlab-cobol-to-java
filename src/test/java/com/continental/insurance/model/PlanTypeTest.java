package com.continental.insurance.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for PlanType enum fromCode method.
 */
class PlanTypeTest {

    @Test
    @DisplayName("fromCode 'BS' returns BASIC")
    void fromCodeBSReturnsBasic() {
        assertEquals(PlanType.BASIC, PlanType.fromCode("BS"));
    }

    @Test
    @DisplayName("fromCode 'SV' returns SILVER")
    void fromCodeSVReturnsSilver() {
        assertEquals(PlanType.SILVER, PlanType.fromCode("SV"));
    }

    @Test
    @DisplayName("fromCode 'PR' returns PREMIUM")
    void fromCodePRReturnsPremium() {
        assertEquals(PlanType.PREMIUM, PlanType.fromCode("PR"));
    }

    @Test
    @DisplayName("fromCode 'BR' returns BRONZE")
    void fromCodeBRReturnsBronze() {
        assertEquals(PlanType.BRONZE, PlanType.fromCode("BR"));
    }

    @Test
    @DisplayName("fromCode throws for unknown code")
    void fromCodeThrowsForUnknown() {
        assertThrows(IllegalArgumentException.class, () -> PlanType.fromCode("XX"));
    }

    @Test
    @DisplayName("fromCode throws for null")
    void fromCodeThrowsForNull() {
        assertThrows(Exception.class, () -> PlanType.fromCode(null));
    }

    @Test
    @DisplayName("getCode returns correct code for each type")
    void getCodeReturnsCorrectValues() {
        assertEquals("BS", PlanType.BASIC.getCode());
        assertEquals("SV", PlanType.SILVER.getCode());
        assertEquals("PR", PlanType.PREMIUM.getCode());
        assertEquals("BR", PlanType.BRONZE.getCode());
    }
}
