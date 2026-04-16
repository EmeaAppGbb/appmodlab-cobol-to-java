package com.continental.insurance.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for ClaimType enum fromCode method.
 */
class ClaimTypeTest {

    @Test
    @DisplayName("fromCode '01' returns MEDICAL")
    void fromCode01ReturnsMedical() {
        assertEquals(ClaimType.MEDICAL, ClaimType.fromCode("01"));
    }

    @Test
    @DisplayName("fromCode '02' returns DENTAL")
    void fromCode02ReturnsDental() {
        assertEquals(ClaimType.DENTAL, ClaimType.fromCode("02"));
    }

    @Test
    @DisplayName("fromCode '03' returns VISION")
    void fromCode03ReturnsVision() {
        assertEquals(ClaimType.VISION, ClaimType.fromCode("03"));
    }

    @Test
    @DisplayName("fromCode '04' returns PHARMACY")
    void fromCode04ReturnsPharmacy() {
        assertEquals(ClaimType.PHARMACY, ClaimType.fromCode("04"));
    }

    @Test
    @DisplayName("fromCode throws for unknown code")
    void fromCodeThrowsForUnknown() {
        assertThrows(IllegalArgumentException.class, () -> ClaimType.fromCode("99"));
    }

    @Test
    @DisplayName("fromCode throws for null")
    void fromCodeThrowsForNull() {
        assertThrows(Exception.class, () -> ClaimType.fromCode(null));
    }

    @Test
    @DisplayName("getCode returns correct code for each type")
    void getCodeReturnsCorrectValues() {
        assertEquals("01", ClaimType.MEDICAL.getCode());
        assertEquals("02", ClaimType.DENTAL.getCode());
        assertEquals("03", ClaimType.VISION.getCode());
        assertEquals("04", ClaimType.PHARMACY.getCode());
    }
}
