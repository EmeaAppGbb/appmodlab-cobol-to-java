package com.continental.insurance.service;

import com.continental.insurance.model.Claim;
import com.continental.insurance.model.ClaimStatus;
import com.continental.insurance.model.ClaimType;
import com.continental.insurance.model.PlanType;
import com.continental.insurance.model.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Adjudication rules engine — translated from COBOL ADJUDCTN.cbl.
 * <p>
 * Applies business rules to determine whether a claim is APPROVED, DENIED,
 * or PENDING manual review. The original COBOL used GOTO-based control flow;
 * this translation uses structured early-return logic.
 * <p>
 * Business rule constants are externalized in application.yml instead of
 * being hardcoded as in the original COBOL program.
 */
@Service
public class AdjudicationService {

    private static final Logger log = LoggerFactory.getLogger(AdjudicationService.class);

    private static final String ACTIVE_STATUS = "A";
    private static final String EMPTY_DIAGNOSIS = "00000";

    @Value("${claims.adjudication.min-claim-amount}")
    private BigDecimal minClaimAmount;

    @Value("${claims.adjudication.max-auto-approve}")
    private BigDecimal maxAutoApprove;

    @Value("${claims.adjudication.manual-review-limit}")
    private BigDecimal manualReviewLimit;

    @Value("${claims.adjudication.dental-max}")
    private BigDecimal dentalMax;

    @Value("${claims.adjudication.vision-max}")
    private BigDecimal visionMax;

    /**
     * Adjudicate a claim against its policy.
     * Translates COBOL 0000-ADJUDICATE-CLAIM through 9900-APPROVE-CLAIM.
     *
     * @param claim  the claim to adjudicate
     * @param policy the associated policy
     * @return the adjudication result (APPROVED, DENIED, or PENDING)
     */
    public ClaimStatus adjudicate(Claim claim, Policy policy) {
        log.info("Adjudicating claim {} against policy {}", claim.getClaimNumber(), policy.getPolicyNumber());

        // 1000-VALIDATE-POLICY-STATUS
        if (!isPolicyActive(claim, policy)) {
            log.info("Claim {} denied: policy not active or dates invalid", claim.getClaimNumber());
            return ClaimStatus.DENIED;
        }

        // 2000-CHECK-CLAIM-AMOUNT
        ClaimStatus amountCheck = checkClaimAmount(claim);
        if (amountCheck != null) {
            return amountCheck;
        }

        // 3000-CHECK-CLAIM-TYPE
        ClaimStatus typeCheck = checkClaimType(claim, policy);
        if (typeCheck != null) {
            return typeCheck;
        }

        // 4000-APPLY-COVERAGE-RULES
        return applyCoverageRules(claim, policy);
    }

    /**
     * Validate policy status and date range.
     * Translates 1000-VALIDATE-POLICY-STATUS.
     */
    private boolean isPolicyActive(Claim claim, Policy policy) {
        if (!ACTIVE_STATUS.equals(policy.getPolicyStatus())) {
            return false;
        }

        if (claim.getClaimDate().isBefore(policy.getEffectiveDate())) {
            return false;
        }

        if (claim.getClaimDate().isAfter(policy.getExpiryDate())) {
            return false;
        }

        return true;
    }

    /**
     * Check claim amount against thresholds.
     * Translates 2000-CHECK-CLAIM-AMOUNT.
     *
     * @return DENIED if below minimum, PENDING if above manual review limit, null to continue
     */
    private ClaimStatus checkClaimAmount(Claim claim) {
        BigDecimal amount = claim.getClaimAmount();

        if (amount.compareTo(minClaimAmount) < 0) {
            log.info("Claim {} denied: amount {} below minimum {}", claim.getClaimNumber(), amount, minClaimAmount);
            return ClaimStatus.DENIED;
        }

        if (amount.compareTo(manualReviewLimit) > 0) {
            log.info("Claim {} pending: amount {} exceeds manual review limit {}", claim.getClaimNumber(), amount, manualReviewLimit);
            return ClaimStatus.PENDING;
        }

        return null;
    }

    /**
     * Check claim-type-specific rules.
     * Translates 3000-CHECK-CLAIM-TYPE and sub-paragraphs 3100–3400.
     *
     * @return DENIED if rules violated, null to continue
     */
    private ClaimStatus checkClaimType(Claim claim, Policy policy) {
        ClaimType type = claim.getClaimType();

        if (type == null) {
            log.info("Claim {} denied: unknown claim type", claim.getClaimNumber());
            return ClaimStatus.DENIED;
        }

        switch (type) {
            case MEDICAL:
                return checkMedicalRules(claim);
            case DENTAL:
                return checkDentalRules(claim, policy);
            case VISION:
                return checkVisionRules(claim);
            case PHARMACY:
                return checkPharmacyRules(claim);
            default:
                log.info("Claim {} denied: unrecognized claim type {}", claim.getClaimNumber(), type);
                return ClaimStatus.DENIED;
        }
    }

    /**
     * 3100-CHECK-MEDICAL-RULES: requires diagnosis code and provider ID.
     */
    private ClaimStatus checkMedicalRules(Claim claim) {
        if (isBlankOrEmpty(claim.getDiagnosisCode()) || EMPTY_DIAGNOSIS.equals(claim.getDiagnosisCode())) {
            log.info("Claim {} denied: medical claim missing diagnosis code", claim.getClaimNumber());
            return ClaimStatus.DENIED;
        }

        if (isBlankOrEmpty(claim.getProviderId())) {
            log.info("Claim {} denied: medical claim missing provider ID", claim.getClaimNumber());
            return ClaimStatus.DENIED;
        }

        return null;
    }

    /**
     * 3200-CHECK-DENTAL-RULES: dental has a lower maximum unless Premium plan.
     */
    private ClaimStatus checkDentalRules(Claim claim, Policy policy) {
        if (claim.getClaimAmount().compareTo(dentalMax) > 0) {
            if (policy.getPlanType() != PlanType.PREMIUM) {
                log.info("Claim {} denied: dental amount {} exceeds max {} for non-Premium plan",
                        claim.getClaimNumber(), claim.getClaimAmount(), dentalMax);
                return ClaimStatus.DENIED;
            }
        }
        return null;
    }

    /**
     * 3300-CHECK-VISION-RULES: vision has strict limits.
     */
    private ClaimStatus checkVisionRules(Claim claim) {
        if (claim.getClaimAmount().compareTo(visionMax) > 0) {
            log.info("Claim {} denied: vision amount {} exceeds max {}",
                    claim.getClaimNumber(), claim.getClaimAmount(), visionMax);
            return ClaimStatus.DENIED;
        }
        return null;
    }

    /**
     * 3400-CHECK-PHARMACY-RULES: pharmacy requires provider.
     */
    private ClaimStatus checkPharmacyRules(Claim claim) {
        if (isBlankOrEmpty(claim.getProviderId())) {
            log.info("Claim {} denied: pharmacy claim missing provider ID", claim.getClaimNumber());
            return ClaimStatus.DENIED;
        }
        return null;
    }

    /**
     * Apply coverage rules: calculate coverage after deductible.
     * Translates 4000-APPLY-COVERAGE-RULES.
     */
    private ClaimStatus applyCoverageRules(Claim claim, Policy policy) {
        BigDecimal calculatedCoverage = claim.getClaimAmount().subtract(policy.getDeductible());

        if (calculatedCoverage.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("Claim {} denied: coverage after deductible is non-positive", claim.getClaimNumber());
            return ClaimStatus.DENIED;
        }

        if (calculatedCoverage.compareTo(policy.getMaxCoverage()) > 0) {
            log.info("Claim {} pending: calculated coverage {} exceeds max coverage {}",
                    claim.getClaimNumber(), calculatedCoverage, policy.getMaxCoverage());
            return ClaimStatus.PENDING;
        }

        if (claim.getClaimAmount().compareTo(maxAutoApprove) <= 0) {
            log.info("Claim {} approved", claim.getClaimNumber());
            return ClaimStatus.APPROVED;
        }

        log.info("Claim {} pending: amount {} exceeds auto-approve threshold {}",
                claim.getClaimNumber(), claim.getClaimAmount(), maxAutoApprove);
        return ClaimStatus.PENDING;
    }

    private boolean isBlankOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
