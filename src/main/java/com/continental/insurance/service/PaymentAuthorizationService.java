package com.continental.insurance.service;

import com.continental.insurance.model.Claim;
import com.continental.insurance.model.ClaimType;
import com.continental.insurance.model.Payment;
import com.continental.insurance.model.PaymentStatus;
import com.continental.insurance.model.PlanType;
import com.continental.insurance.model.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Payment authorization service — translated from COBOL PYMTAUTH.cbl.
 * <p>
 * Calculates the net payment amount for an approved claim based on
 * the plan's coinsurance rate, applicable copays, and the policy deductible.
 * <p>
 * Formula (from COBOL 2300/2400):
 *   net = (grossAmount - deductibleApplied - copay) * (coinsuranceRate / 100)
 * Then capped by policy max coverage and claim amount.
 */
@Service
public class PaymentAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentAuthorizationService.class);

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    // Coinsurance rates by plan type (from application.yml)
    @Value("${claims.coinsurance.basic}")
    private int coinsuranceBasic;

    @Value("${claims.coinsurance.silver}")
    private int coinsuranceSilver;

    @Value("${claims.coinsurance.premium}")
    private int coinsurancePremium;

    @Value("${claims.coinsurance.bronze}")
    private int coinsuranceBronze;

    @Value("${claims.coinsurance.default-rate}")
    private int coinsuranceDefault;

    // Copay amounts by claim type (from application.yml)
    @Value("${claims.copay.medical}")
    private BigDecimal copayMedical;

    @Value("${claims.copay.dental}")
    private BigDecimal copayDental;

    @Value("${claims.copay.vision}")
    private BigDecimal copayVision;

    @Value("${claims.copay.pharmacy}")
    private BigDecimal copayPharmacy;

    private final AtomicLong authCounter = new AtomicLong(0);

    /**
     * Authorize payment for an approved claim.
     * Translates COBOL 0000-MAIN-AUTHORIZATION through 9000-FINALIZE.
     *
     * @param claim  the approved claim
     * @param policy the associated policy
     * @return a Payment record with the calculated net amount and authorization code
     */
    public Payment authorizePayment(Claim claim, Policy policy) {
        log.info("Authorizing payment for claim {}", claim.getClaimNumber());

        // 1000-INITIALIZE-PAYMENT / 2000-CALCULATE-PAYMENT
        BigDecimal grossAmount = claim.getClaimAmount();

        // 2000 - Determine coinsurance rate by plan type
        BigDecimal coinsuranceRate = getCoinsuranceRate(policy.getPlanType());

        // 2100-APPLY-DEDUCTIBLE
        BigDecimal deductibleApplied = applyDeductible(grossAmount, policy.getDeductible());

        // 2200-APPLY-COPAY
        BigDecimal copayAmount = getCopayAmount(claim.getClaimType());

        // 2300-CALCULATE-COINSURANCE
        BigDecimal afterDeductions = grossAmount.subtract(deductibleApplied).subtract(copayAmount);
        BigDecimal coinsuranceAmount = afterDeductions.multiply(coinsuranceRate)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

        if (coinsuranceAmount.compareTo(BigDecimal.ZERO) < 0) {
            coinsuranceAmount = BigDecimal.ZERO;
        }

        // 2400-COMPUTE-NET-PAYMENT
        BigDecimal netPayment = coinsuranceAmount;

        // 3000-APPLY-LIMITS
        netPayment = applyLimits(netPayment, policy.getMaxCoverage(), claim.getClaimAmount());

        // 4000-GENERATE-AUTH-CODE
        Payment payment = new Payment();
        payment.setClaimNumber(claim.getClaimNumber());
        payment.setPaymentAmount(netPayment);
        payment.setPaymentDate(LocalDate.now());
        payment.setAuthCode(generateAuthCode());
        payment.setStatus(PaymentStatus.AUTHORIZED);

        log.info("Payment authorized for claim {}: ${}", claim.getClaimNumber(), netPayment);
        return payment;
    }

    /**
     * Determine coinsurance rate by plan type.
     * Translates COBOL EVALUATE POL-PLAN-TYPE in 2000-CALCULATE-PAYMENT.
     */
    private BigDecimal getCoinsuranceRate(PlanType planType) {
        if (planType == null) {
            return new BigDecimal(coinsuranceDefault);
        }

        switch (planType) {
            case BASIC:
                return new BigDecimal(coinsuranceBasic);
            case SILVER:
                return new BigDecimal(coinsuranceSilver);
            case PREMIUM:
                return new BigDecimal(coinsurancePremium);
            case BRONZE:
                return new BigDecimal(coinsuranceBronze);
            default:
                return new BigDecimal(coinsuranceDefault);
        }
    }

    /**
     * Apply deductible — the lesser of gross amount and the policy deductible.
     * Translates 2100-APPLY-DEDUCTIBLE.
     */
    private BigDecimal applyDeductible(BigDecimal grossAmount, BigDecimal deductible) {
        if (deductible.compareTo(BigDecimal.ZERO) > 0) {
            return grossAmount.min(deductible);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Determine copay amount by claim type.
     * Translates 2200-APPLY-COPAY.
     */
    private BigDecimal getCopayAmount(ClaimType claimType) {
        if (claimType == null) {
            return BigDecimal.ZERO;
        }

        switch (claimType) {
            case MEDICAL:
                return copayMedical;
            case DENTAL:
                return copayDental;
            case VISION:
                return copayVision;
            case PHARMACY:
                return copayPharmacy;
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * Apply payment limits — cap by max coverage, claim amount, and floor at zero.
     * Translates 3000-APPLY-LIMITS.
     */
    private BigDecimal applyLimits(BigDecimal netPayment, BigDecimal maxCoverage, BigDecimal claimAmount) {
        if (netPayment.compareTo(maxCoverage) > 0) {
            netPayment = maxCoverage;
        }

        if (netPayment.compareTo(claimAmount) > 0) {
            netPayment = claimAmount;
        }

        if (netPayment.compareTo(BigDecimal.ZERO) < 0) {
            netPayment = BigDecimal.ZERO;
        }

        return netPayment;
    }

    /**
     * Generate a unique 6-character authorization code.
     * Translates 4000-GENERATE-AUTH-CODE.
     */
    private String generateAuthCode() {
        long counter = authCounter.incrementAndGet();
        return String.format("A%05d", counter % 100000);
    }
}
