package com.continental.insurance.service;

import com.continental.insurance.model.Claim;
import com.continental.insurance.model.ClaimType;
import com.continental.insurance.model.Payment;
import com.continental.insurance.model.PaymentStatus;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for PaymentAuthorizationService — covers payment calculation rules
 * translated from COBOL PYMTAUTH.cbl.
 */
class PaymentAuthorizationServiceTest {

    private PaymentAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new PaymentAuthorizationService();
        // Coinsurance rates
        ReflectionTestUtils.setField(service, "coinsuranceBasic", 80);
        ReflectionTestUtils.setField(service, "coinsuranceSilver", 70);
        ReflectionTestUtils.setField(service, "coinsurancePremium", 90);
        ReflectionTestUtils.setField(service, "coinsuranceBronze", 60);
        ReflectionTestUtils.setField(service, "coinsuranceDefault", 80);
        // Copay amounts
        ReflectionTestUtils.setField(service, "copayMedical", new BigDecimal("25.00"));
        ReflectionTestUtils.setField(service, "copayDental", new BigDecimal("15.00"));
        ReflectionTestUtils.setField(service, "copayVision", new BigDecimal("10.00"));
        ReflectionTestUtils.setField(service, "copayPharmacy", new BigDecimal("10.00"));
    }

    // ---- helpers ----

    private Policy policy(PlanType plan, BigDecimal deductible, BigDecimal maxCoverage) {
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

    private Claim claim(ClaimType type, BigDecimal amount) {
        Claim c = new Claim();
        c.setClaimNumber("CLM0000001");
        c.setPolicyNumber("POL0000001");
        c.setClaimDate(LocalDate.of(2025, 6, 15));
        c.setClaimType(type);
        c.setClaimAmount(amount);
        c.setDiagnosisCode("A1234");
        c.setProviderId("PROV0001");
        return c;
    }

    /**
     * Calculate expected: net = (gross - min(gross,deductible) - copay) * rate/100
     */
    private BigDecimal expectedNet(BigDecimal gross, BigDecimal deductible, BigDecimal copay, int rate) {
        BigDecimal dedApplied = gross.min(deductible);
        BigDecimal afterDeductions = gross.subtract(dedApplied).subtract(copay);
        BigDecimal net = afterDeductions.multiply(new BigDecimal(rate))
                .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        return net.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : net;
    }

    // ---- Coinsurance rate tests ----

    @Nested
    @DisplayName("Coinsurance rates by plan type")
    class CoinsuranceRates {

        @Test
        @DisplayName("Basic plan uses 80% coinsurance")
        void basicPlan80Percent() {
            Claim c = claim(ClaimType.MEDICAL, new BigDecimal("1000.00"));
            Policy p = policy(PlanType.BASIC, new BigDecimal("200.00"), new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);
            // net = (1000 - 200 - 25) * 80/100 = 775 * 0.80 = 620.00
            assertEquals(new BigDecimal("620.00"), payment.getPaymentAmount());
        }

        @Test
        @DisplayName("Silver plan uses 70% coinsurance")
        void silverPlan70Percent() {
            Claim c = claim(ClaimType.MEDICAL, new BigDecimal("1000.00"));
            Policy p = policy(PlanType.SILVER, new BigDecimal("200.00"), new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);
            // net = (1000 - 200 - 25) * 70/100 = 775 * 0.70 = 542.50
            assertEquals(new BigDecimal("542.50"), payment.getPaymentAmount());
        }

        @Test
        @DisplayName("Premium plan uses 90% coinsurance")
        void premiumPlan90Percent() {
            Claim c = claim(ClaimType.MEDICAL, new BigDecimal("1000.00"));
            Policy p = policy(PlanType.PREMIUM, new BigDecimal("200.00"), new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);
            // net = (1000 - 200 - 25) * 90/100 = 775 * 0.90 = 697.50
            assertEquals(new BigDecimal("697.50"), payment.getPaymentAmount());
        }

        @Test
        @DisplayName("Bronze plan uses 60% coinsurance")
        void bronzePlan60Percent() {
            Claim c = claim(ClaimType.MEDICAL, new BigDecimal("1000.00"));
            Policy p = policy(PlanType.BRONZE, new BigDecimal("200.00"), new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);
            // net = (1000 - 200 - 25) * 60/100 = 775 * 0.60 = 465.00
            assertEquals(new BigDecimal("465.00"), payment.getPaymentAmount());
        }
    }

    // ---- Copay tests ----

    @Nested
    @DisplayName("Copay amounts by claim type")
    class CopayAmounts {

        @Test
        @DisplayName("Medical copay is $25")
        void medicalCopay() {
            Claim c = claim(ClaimType.MEDICAL, new BigDecimal("500.00"));
            Policy p = policy(PlanType.BASIC, BigDecimal.ZERO, new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);
            // net = (500 - 0 - 25) * 80/100 = 475 * 0.80 = 380.00
            assertEquals(new BigDecimal("380.00"), payment.getPaymentAmount());
        }

        @Test
        @DisplayName("Dental copay is $15")
        void dentalCopay() {
            Claim c = claim(ClaimType.DENTAL, new BigDecimal("500.00"));
            Policy p = policy(PlanType.BASIC, BigDecimal.ZERO, new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);
            // net = (500 - 0 - 15) * 80/100 = 485 * 0.80 = 388.00
            assertEquals(new BigDecimal("388.00"), payment.getPaymentAmount());
        }

        @Test
        @DisplayName("Vision copay is $10")
        void visionCopay() {
            Claim c = claim(ClaimType.VISION, new BigDecimal("500.00"));
            Policy p = policy(PlanType.BASIC, BigDecimal.ZERO, new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);
            // net = (500 - 0 - 10) * 80/100 = 490 * 0.80 = 392.00
            assertEquals(new BigDecimal("392.00"), payment.getPaymentAmount());
        }

        @Test
        @DisplayName("Pharmacy copay is $10")
        void pharmacyCopay() {
            Claim c = claim(ClaimType.PHARMACY, new BigDecimal("500.00"));
            Policy p = policy(PlanType.BASIC, BigDecimal.ZERO, new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);
            // net = (500 - 0 - 10) * 80/100 = 490 * 0.80 = 392.00
            assertEquals(new BigDecimal("392.00"), payment.getPaymentAmount());
        }
    }

    // ---- Deductible tests ----

    @Nested
    @DisplayName("Deductible application")
    class DeductibleApplication {

        @Test
        @DisplayName("Deductible applied correctly when less than gross")
        void deductibleLessThanGross() {
            Claim c = claim(ClaimType.MEDICAL, new BigDecimal("2000.00"));
            Policy p = policy(PlanType.BASIC, new BigDecimal("300.00"), new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);
            // net = (2000 - 300 - 25) * 80/100 = 1675 * 0.80 = 1340.00
            assertEquals(new BigDecimal("1340.00"), payment.getPaymentAmount());
        }

        @Test
        @DisplayName("Deductible capped at gross amount when deductible exceeds gross")
        void deductibleExceedsGross() {
            Claim c = claim(ClaimType.MEDICAL, new BigDecimal("100.00"));
            Policy p = policy(PlanType.BASIC, new BigDecimal("500.00"), new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);
            // deductible applied = min(100, 500) = 100
            // net = (100 - 100 - 25) * 80/100 = -25 * 0.80 = -20.00 → clamped to 0
            assertEquals(0, payment.getPaymentAmount().compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Zero deductible means no deduction")
        void zeroDeductible() {
            Claim c = claim(ClaimType.MEDICAL, new BigDecimal("1000.00"));
            Policy p = policy(PlanType.BASIC, BigDecimal.ZERO, new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);
            // net = (1000 - 0 - 25) * 80/100 = 975 * 0.80 = 780.00
            assertEquals(new BigDecimal("780.00"), payment.getPaymentAmount());
        }
    }

    // ---- Limits tests ----

    @Nested
    @DisplayName("Payment limits")
    class PaymentLimits {

        @Test
        @DisplayName("Payment capped at max coverage")
        void cappedAtMaxCoverage() {
            Claim c = claim(ClaimType.MEDICAL, new BigDecimal("10000.00"));
            Policy p = policy(PlanType.PREMIUM, BigDecimal.ZERO, new BigDecimal("100.00"));

            Payment payment = service.authorizePayment(c, p);
            // net = (10000 - 0 - 25) * 90/100 = 8977.50, but maxCoverage = 100
            assertEquals(new BigDecimal("100.00"), payment.getPaymentAmount());
        }

        @Test
        @DisplayName("Payment capped at claim amount")
        void cappedAtClaimAmount() {
            // Use zero deductible and zero copay scenario where coinsurance could theoretically
            // be tested; but coinsurance is always <=100% so net won't exceed gross.
            // Instead verify net does not exceed claim amount with a small claim.
            Claim c = claim(ClaimType.MEDICAL, new BigDecimal("100.00"));
            Policy p = policy(PlanType.PREMIUM, BigDecimal.ZERO, new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);
            // net = (100 - 0 - 25) * 90/100 = 67.50; 67.50 <= 100 so no cap needed
            assertTrue(payment.getPaymentAmount().compareTo(c.getClaimAmount()) <= 0);
        }

        @Test
        @DisplayName("Payment floored at zero (never negative)")
        void zeroMinimum() {
            Claim c = claim(ClaimType.MEDICAL, new BigDecimal("50.00"));
            Policy p = policy(PlanType.BASIC, new BigDecimal("500.00"), new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);
            // deductible applied = min(50, 500) = 50
            // net = (50 - 50 - 25) * 80/100 = -25 * 0.80 = -20.00 → clamped to 0
            assertEquals(0, payment.getPaymentAmount().compareTo(BigDecimal.ZERO));
        }
    }

    // ---- Payment record tests ----

    @Nested
    @DisplayName("Payment record fields")
    class PaymentRecord {

        @Test
        @DisplayName("Payment has AUTHORIZED status")
        void paymentStatusAuthorized() {
            Claim c = claim(ClaimType.MEDICAL, new BigDecimal("1000.00"));
            Policy p = policy(PlanType.BASIC, new BigDecimal("100.00"), new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);

            assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());
        }

        @Test
        @DisplayName("Payment has auth code and claim number")
        void paymentHasAuthCodeAndClaimNumber() {
            Claim c = claim(ClaimType.MEDICAL, new BigDecimal("1000.00"));
            Policy p = policy(PlanType.BASIC, new BigDecimal("100.00"), new BigDecimal("50000.00"));

            Payment payment = service.authorizePayment(c, p);

            assertNotNull(payment.getAuthCode());
            assertEquals("CLM0000001", payment.getClaimNumber());
            assertNotNull(payment.getPaymentDate());
        }

        @Test
        @DisplayName("Auth codes are unique across calls")
        void authCodesUnique() {
            Claim c = claim(ClaimType.MEDICAL, new BigDecimal("1000.00"));
            Policy p = policy(PlanType.BASIC, new BigDecimal("100.00"), new BigDecimal("50000.00"));

            Payment p1 = service.authorizePayment(c, p);
            Payment p2 = service.authorizePayment(c, p);

            assertNotNull(p1.getAuthCode());
            assertNotNull(p2.getAuthCode());
            assertTrue(!p1.getAuthCode().equals(p2.getAuthCode()),
                    "Auth codes should be unique");
        }
    }
}
