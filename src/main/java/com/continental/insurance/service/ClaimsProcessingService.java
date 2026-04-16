package com.continental.insurance.service;

import com.continental.insurance.exception.PolicyNotFoundException;
import com.continental.insurance.model.Claim;
import com.continental.insurance.model.ClaimStatus;
import com.continental.insurance.model.Payment;
import com.continental.insurance.model.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Main claims processing orchestrator — translated from COBOL CLMPROC.cbl.
 * <p>
 * Coordinates the full claims processing pipeline:
 * 1. Read claims
 * 2. Look up associated policy (POLYLKUP)
 * 3. Adjudicate claim (ADJUDCTN)
 * 4. Authorize payment for approved claims (PYMTAUTH)
 * 5. Generate summary report (RPTGEN)
 */
@Service
public class ClaimsProcessingService {

    private static final Logger log = LoggerFactory.getLogger(ClaimsProcessingService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PolicyLookupService policyLookupService;

    @Autowired
    private AdjudicationService adjudicationService;

    @Autowired
    private PaymentAuthorizationService paymentAuthorizationService;

    @Autowired
    private ReportGenerationService reportGenerationService;

    /**
     * Result of processing a batch of claims.
     */
    public static class ProcessingResult {
        private final int claimsRead;
        private final int claimsApproved;
        private final int claimsDenied;
        private final int claimsPending;
        private final BigDecimal totalPaid;
        private final List<Payment> payments;
        private final ReportGenerationService.SummaryReport summaryReport;

        public ProcessingResult(int claimsRead, int claimsApproved, int claimsDenied,
                                int claimsPending, BigDecimal totalPaid,
                                List<Payment> payments,
                                ReportGenerationService.SummaryReport summaryReport) {
            this.claimsRead = claimsRead;
            this.claimsApproved = claimsApproved;
            this.claimsDenied = claimsDenied;
            this.claimsPending = claimsPending;
            this.totalPaid = totalPaid;
            this.payments = payments;
            this.summaryReport = summaryReport;
        }

        public int getClaimsRead() { return claimsRead; }
        public int getClaimsApproved() { return claimsApproved; }
        public int getClaimsDenied() { return claimsDenied; }
        public int getClaimsPending() { return claimsPending; }
        public BigDecimal getTotalPaid() { return totalPaid; }
        public List<Payment> getPayments() { return payments; }
        public ReportGenerationService.SummaryReport getSummaryReport() { return summaryReport; }
    }

    /**
     * Process all claims in the database.
     * Translates COBOL 0000-MAIN-PROCESSING through 9000-CLEANUP.
     *
     * @return the processing result with counters and summary report
     */
    @Transactional
    public ProcessingResult processAllClaims() {
        log.info("CONTINENTAL INSURANCE - CLAIMS PROCESSING");
        log.info("INITIALIZING SYSTEM...");

        // 1000-INITIALIZATION: read all claims from the database
        List<Claim> claims = entityManager
                .createQuery("SELECT c FROM Claim c", Claim.class)
                .getResultList();

        log.info("Processing date: {}", java.time.LocalDate.now());
        log.info("Total claims to process: {}", claims.size());

        // 2000-PROCESS-CLAIMS: iterate and process each claim
        int claimsRead = 0;
        int claimsApproved = 0;
        int claimsDenied = 0;
        int claimsPending = 0;
        BigDecimal totalPaid = BigDecimal.ZERO;
        List<Payment> payments = new ArrayList<>();

        for (Claim claim : claims) {
            claimsRead++;
            log.info("PROCESSING CLAIM: {}", claim.getClaimNumber());

            // 2100-PROCESS-SINGLE-CLAIM
            ClaimStatus result = processSingleClaim(claim, payments);

            switch (result) {
                case APPROVED:
                    claimsApproved++;
                    break;
                case DENIED:
                    claimsDenied++;
                    break;
                case PENDING:
                    claimsPending++;
                    break;
            }
        }

        // Sum total paid from authorized payments
        for (Payment payment : payments) {
            totalPaid = totalPaid.add(payment.getPaymentAmount());
        }

        // 3000-GENERATE-SUMMARY
        ReportGenerationService.SummaryReport summary = reportGenerationService.generateSummary(
                claimsRead, claimsApproved, claimsDenied, claimsPending, totalPaid);

        // 9000-CLEANUP
        log.info("PROCESSING COMPLETE");
        log.info("TOTAL CLAIMS PROCESSED: {}", claimsRead);
        log.info("APPROVED: {}", claimsApproved);
        log.info("DENIED:   {}", claimsDenied);
        log.info("PENDING:  {}", claimsPending);
        log.info("TOTAL PAID: ${}", totalPaid);

        return new ProcessingResult(claimsRead, claimsApproved, claimsDenied,
                claimsPending, totalPaid, payments, summary);
    }

    /**
     * Process a single claim: lookup policy, adjudicate, authorize payment if approved.
     * Translates 2100-PROCESS-SINGLE-CLAIM and 2200-AUTHORIZE-PAYMENT.
     *
     * @param claim    the claim to process
     * @param payments list to which authorized payments are appended
     * @return the resulting claim status
     */
    @Transactional
    public ClaimStatus processSingleClaim(Claim claim, List<Payment> payments) {
        // POLYLKUP — look up the policy
        Policy policy;
        try {
            policy = policyLookupService.lookupPolicy(claim.getPolicyNumber());
        } catch (PolicyNotFoundException e) {
            log.warn("  POLICY NOT FOUND: {}", claim.getPolicyNumber());
            claim.setStatus(ClaimStatus.DENIED);
            entityManager.merge(claim);
            return ClaimStatus.DENIED;
        }

        // ADJUDCTN — adjudicate the claim
        ClaimStatus result = adjudicationService.adjudicate(claim, policy);
        claim.setStatus(result);

        switch (result) {
            case APPROVED:
                log.info("  CLAIM APPROVED");
                // 2200-AUTHORIZE-PAYMENT
                Payment payment = paymentAuthorizationService.authorizePayment(claim, policy);
                if (payment.getPaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
                    entityManager.persist(payment);
                    payments.add(payment);
                    log.info("  PAYMENT AUTHORIZED: ${}", payment.getPaymentAmount());
                }
                break;
            case DENIED:
                log.info("  CLAIM DENIED");
                break;
            case PENDING:
                log.info("  CLAIM PENDING REVIEW");
                break;
        }

        entityManager.merge(claim);
        return result;
    }
}
