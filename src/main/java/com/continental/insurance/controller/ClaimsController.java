package com.continental.insurance.controller;

import com.continental.insurance.model.Claim;
import com.continental.insurance.model.ClaimStatus;
import com.continental.insurance.model.Payment;
import com.continental.insurance.model.Policy;
import com.continental.insurance.repository.ClaimRepository;
import com.continental.insurance.repository.PaymentRepository;
import com.continental.insurance.service.AdjudicationService;
import com.continental.insurance.service.ClaimsProcessingService;
import com.continental.insurance.service.PaymentAuthorizationService;
import com.continental.insurance.service.PolicyLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for claims operations.
 * Exposes the claims processing pipeline as HTTP endpoints.
 */
@RestController
@RequestMapping("/api/claims")
public class ClaimsController {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ClaimsProcessingService claimsProcessingService;

    @Autowired
    private PolicyLookupService policyLookupService;

    @Autowired
    private AdjudicationService adjudicationService;

    @Autowired
    private PaymentAuthorizationService paymentAuthorizationService;

    /**
     * GET /api/claims — list all claims.
     */
    @GetMapping
    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    /**
     * GET /api/claims/{id} — get a single claim by claim number.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Claim> getClaimById(@PathVariable String id) {
        return claimRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/claims/process — process all pending claims through the full pipeline.
     * Translates the batch processing from COBOL CLMPROC main paragraph.
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processAllClaims() {
        ClaimsProcessingService.ProcessingResult result = claimsProcessingService.processAllClaims();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("claimsRead", result.getClaimsRead());
        response.put("claimsApproved", result.getClaimsApproved());
        response.put("claimsDenied", result.getClaimsDenied());
        response.put("claimsPending", result.getClaimsPending());
        response.put("totalPaid", result.getTotalPaid());
        response.put("paymentsGenerated", result.getPayments().size());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/claims/{id}/adjudicate — adjudicate a single claim.
     * Looks up the policy, runs adjudication rules, and authorizes payment if approved.
     */
    @PostMapping("/{id}/adjudicate")
    public ResponseEntity<Map<String, Object>> adjudicateClaim(@PathVariable String id) {
        Claim claim = claimRepository.findById(id).orElse(null);
        if (claim == null) {
            return ResponseEntity.notFound().build();
        }

        Policy policy;
        try {
            policy = policyLookupService.lookupPolicy(claim.getPolicyNumber());
        } catch (Exception e) {
            claim.setStatus(ClaimStatus.DENIED);
            claimRepository.save(claim);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("claimNumber", claim.getClaimNumber());
            response.put("status", ClaimStatus.DENIED.name());
            response.put("reason", "Policy not found: " + claim.getPolicyNumber());
            return ResponseEntity.ok(response);
        }

        ClaimStatus result = adjudicationService.adjudicate(claim, policy);
        claim.setStatus(result);
        claimRepository.save(claim);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("claimNumber", claim.getClaimNumber());
        response.put("status", result.name());

        if (result == ClaimStatus.APPROVED) {
            Payment payment = paymentAuthorizationService.authorizePayment(claim, policy);
            paymentRepository.save(payment);
            response.put("paymentAmount", payment.getPaymentAmount());
            response.put("authCode", payment.getAuthCode());
        }

        return ResponseEntity.ok(response);
    }
}
