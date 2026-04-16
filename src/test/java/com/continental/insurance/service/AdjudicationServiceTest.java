package com.continental.insurance.service;

import com.continental.insurance.model.Claim;
import com.continental.insurance.model.ClaimStatus;
import com.continental.insurance.model.ClaimType;
import com.continental.insurance.model.PlanType;
import com.continental.insurance.model.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for AdjudicationService — covers all adjudication business rules
 * translated from COBOL ADJUDCTN.cbl.
 */
class AdjudicationServiceTest {

    private AdjudicationService service;

    @BeforeEach
    void setUp() {
        service = new AdjudicationService();
        // Inject @Value properties matching application.yml defaults
        ReflectionTestUtils.setField(service, "minClaimAmount", new BigDecimal("50.00"));
        ReflectionTestUtils.setField(service, "maxAutoApprove", new BigDecimal("5000.00"));
        ReflectionTestUtils.setField(service, "manualReviewLimit", new BigDecimal("25000.00"));
        ReflectionTestUtils.setField(service, "dentalMax", new BigDecimal("2500.00"));
        ReflectionTestUtils.setField(service, "visionMax", new BigDecimal("500.00"));
    }

    // ---- helpers ----

    private Policy activePolicy(PlanType plan, BigDecimal deductible, BigDecimal maxCoverage) {
        Policy p = new Policy();
        p.setPolicyNumber("POL0000001");
        p.setHolderName("Test Holder");
        p.setPlanType(plan);
        p.setEffectiveDate(LocalDate.of(2024, 1, 1));
        p.setExpiryDate(LocalDate.of(2026, 12, 31));
        p.setDeductible(deductible);
        p.setMaxCoverage(maxCoverage);
        p.setPolicyStatus("A");
        return p;
    }

    private Policy activePolicy() {
        return activePolicy(PlanType.BASIC, new BigDecimal("500.00"), new BigDecimal("50000.00"));
    }

    private Claim medicalClaim(BigDecimal amount) {
        Claim c = new Claim();
        c.setClaimNumber("CLM0000001");
        c.setPolicyNumber("POL0000001");
        c.setClaimDate(LocalDate.of(2025, 6, 15));
        c.setClaimType(ClaimType.MEDICAL);
        c.setClaimAmount(amount);
        c.setDiagnosisCode("A1234");
        c.setProviderId("PROV0001");
        return c;
    }

    // ---- Policy validation tests ----

    @Nested
    @DisplayName("Policy validation rules")
    class PolicyValidation {

        @Test
        @DisplayName("Approve claim under 5000 with valid active policy")
        void approveClaimUnder5000WithValidPolicy() {
            Claim claim = medicalClaim(new BigDecimal("3000.00"));
            Policy policy = activePolicy();

            assertEquals(ClaimStatus.APPROVED, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Deny claim when policy is inactive")
        void denyInactivePolicy() {
            Claim claim = medicalClaim(new BigDecimal("1000.00"));
            Policy policy = activePolicy();
            policy.setPolicyStatus("I");

            assertEquals(ClaimStatus.DENIED, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Deny claim when policy is expired (claim date after expiry)")
        void denyExpiredPolicy() {
            Claim claim = medicalClaim(new BigDecimal("1000.00"));
            Policy policy = activePolicy();
            claim.setClaimDate(LocalDate.of(2027, 1, 1));

            assertEquals(ClaimStatus.DENIED, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Deny claim before policy effective date")
        void denyClaimBeforeEffectiveDate() {
            Claim claim = medicalClaim(new BigDecimal("1000.00"));
            Policy policy = activePolicy();
            claim.setClaimDate(LocalDate.of(2023, 12, 31));

            assertEquals(ClaimStatus.DENIED, service.adjudicate(claim, policy));
        }
    }

    // ---- Amount threshold tests ----

    @Nested
    @DisplayName("Claim amount threshold rules")
    class AmountThresholds {

        @Test
        @DisplayName("Deny claim below minimum $50")
        void denyBelowMinimum50() {
            Claim claim = medicalClaim(new BigDecimal("49.99"));
            Policy policy = activePolicy();

            assertEquals(ClaimStatus.DENIED, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Accept claim at exactly minimum $50")
        void acceptAtExactMinimum() {
            Claim claim = medicalClaim(new BigDecimal("50.00"));
            Policy policy = activePolicy();

            // $50 claim, $500 deductible: 50 - 500 = -450, non-positive → DENIED by coverage rules
            assertEquals(ClaimStatus.DENIED, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Send to manual review above $25,000")
        void manualReviewAbove25000() {
            Claim claim = medicalClaim(new BigDecimal("25000.01"));
            Policy policy = activePolicy();

            assertEquals(ClaimStatus.PENDING, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Approve claim at exactly $5000 (max auto-approve)")
        void approveAtExactMaxAutoApprove() {
            Claim claim = medicalClaim(new BigDecimal("5000.00"));
            Policy policy = activePolicy();

            assertEquals(ClaimStatus.APPROVED, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Pending for claim over $5000 but under $25,000")
        void pendingAboveAutoApproveLimit() {
            Claim claim = medicalClaim(new BigDecimal("5000.01"));
            Policy policy = activePolicy();

            // Above maxAutoApprove but below manualReviewLimit → PENDING
            assertEquals(ClaimStatus.PENDING, service.adjudicate(claim, policy));
        }
    }

    // ---- Claim type specific rules ----

    @Nested
    @DisplayName("Medical claim rules")
    class MedicalRules {

        @Test
        @DisplayName("Medical claim needs diagnosis code")
        void medicalNeedsDiagnosisCode() {
            Claim claim = medicalClaim(new BigDecimal("1000.00"));
            claim.setDiagnosisCode(null);
            Policy policy = activePolicy();

            assertEquals(ClaimStatus.DENIED, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Medical claim denied with empty diagnosis '00000'")
        void medicalDeniedWithEmptyDiagnosis() {
            Claim claim = medicalClaim(new BigDecimal("1000.00"));
            claim.setDiagnosisCode("00000");
            Policy policy = activePolicy();

            assertEquals(ClaimStatus.DENIED, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Medical claim needs provider ID")
        void medicalNeedsProvider() {
            Claim claim = medicalClaim(new BigDecimal("1000.00"));
            claim.setProviderId(null);
            Policy policy = activePolicy();

            assertEquals(ClaimStatus.DENIED, service.adjudicate(claim, policy));
        }
    }

    @Nested
    @DisplayName("Dental claim rules")
    class DentalRules {

        @Test
        @DisplayName("Dental over $2500 denied unless Premium plan")
        void dentalOver2500DeniedNonPremium() {
            Claim claim = medicalClaim(new BigDecimal("3000.00"));
            claim.setClaimType(ClaimType.DENTAL);
            Policy policy = activePolicy(PlanType.BASIC, new BigDecimal("100.00"), new BigDecimal("50000.00"));

            assertEquals(ClaimStatus.DENIED, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Dental over $2500 allowed for Premium plan")
        void dentalOver2500AllowedPremium() {
            Claim claim = medicalClaim(new BigDecimal("3000.00"));
            claim.setClaimType(ClaimType.DENTAL);
            Policy policy = activePolicy(PlanType.PREMIUM, new BigDecimal("100.00"), new BigDecimal("50000.00"));

            assertEquals(ClaimStatus.APPROVED, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Dental at or under $2500 approved for any plan")
        void dentalUnder2500Approved() {
            Claim claim = medicalClaim(new BigDecimal("2500.00"));
            claim.setClaimType(ClaimType.DENTAL);
            Policy policy = activePolicy(PlanType.BRONZE, new BigDecimal("100.00"), new BigDecimal("50000.00"));

            assertEquals(ClaimStatus.APPROVED, service.adjudicate(claim, policy));
        }
    }

    @Nested
    @DisplayName("Vision claim rules")
    class VisionRules {

        @Test
        @DisplayName("Vision over $500 denied")
        void visionOver500Denied() {
            Claim claim = medicalClaim(new BigDecimal("500.01"));
            claim.setClaimType(ClaimType.VISION);
            Policy policy = activePolicy();

            assertEquals(ClaimStatus.DENIED, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Vision at $500 approved")
        void visionAt500Approved() {
            Claim claim = medicalClaim(new BigDecimal("500.00"));
            claim.setClaimType(ClaimType.VISION);
            Policy policy = activePolicy(PlanType.BASIC, new BigDecimal("100.00"), new BigDecimal("50000.00"));

            assertEquals(ClaimStatus.APPROVED, service.adjudicate(claim, policy));
        }
    }

    @Nested
    @DisplayName("Pharmacy claim rules")
    class PharmacyRules {

        @Test
        @DisplayName("Pharmacy claim needs provider")
        void pharmacyNeedsProvider() {
            Claim claim = medicalClaim(new BigDecimal("200.00"));
            claim.setClaimType(ClaimType.PHARMACY);
            claim.setProviderId(null);
            Policy policy = activePolicy();

            assertEquals(ClaimStatus.DENIED, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Pharmacy claim with provider approved")
        void pharmacyWithProviderApproved() {
            Claim claim = medicalClaim(new BigDecimal("200.00"));
            claim.setClaimType(ClaimType.PHARMACY);
            claim.setProviderId("PROV0001");
            Policy policy = activePolicy(PlanType.BASIC, new BigDecimal("50.00"), new BigDecimal("50000.00"));

            assertEquals(ClaimStatus.APPROVED, service.adjudicate(claim, policy));
        }
    }

    @Nested
    @DisplayName("Unknown claim type")
    class UnknownType {

        @Test
        @DisplayName("Null claim type denied")
        void nullClaimTypeDenied() {
            Claim claim = medicalClaim(new BigDecimal("1000.00"));
            claim.setClaimType(null);
            Policy policy = activePolicy();

            assertEquals(ClaimStatus.DENIED, service.adjudicate(claim, policy));
        }
    }

    // ---- Coverage rules ----

    @Nested
    @DisplayName("Coverage rules")
    class CoverageRules {

        @Test
        @DisplayName("Deny when claim minus deductible is zero")
        void denyWhenCoverageIsZero() {
            Claim claim = medicalClaim(new BigDecimal("500.00"));
            Policy policy = activePolicy(PlanType.BASIC, new BigDecimal("500.00"), new BigDecimal("50000.00"));

            assertEquals(ClaimStatus.DENIED, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Deny when claim minus deductible is negative")
        void denyWhenCoverageIsNegative() {
            Claim claim = medicalClaim(new BigDecimal("100.00"));
            Policy policy = activePolicy(PlanType.BASIC, new BigDecimal("500.00"), new BigDecimal("50000.00"));

            assertEquals(ClaimStatus.DENIED, service.adjudicate(claim, policy));
        }

        @Test
        @DisplayName("Manual review when calculated coverage exceeds max coverage")
        void pendingWhenExceedsMaxCoverage() {
            // Claim 20000, deductible 100 → coverage = 19900, but maxCoverage = 1000 → PENDING
            Claim claim = medicalClaim(new BigDecimal("20000.00"));
            Policy policy = activePolicy(PlanType.BASIC, new BigDecimal("100.00"), new BigDecimal("1000.00"));

            assertEquals(ClaimStatus.PENDING, service.adjudicate(claim, policy));
        }
    }
}
