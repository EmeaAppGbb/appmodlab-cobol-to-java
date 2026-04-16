package com.continental.insurance.controller;

import com.continental.insurance.model.Claim;
import com.continental.insurance.model.ClaimStatus;
import com.continental.insurance.model.Payment;
import com.continental.insurance.repository.ClaimRepository;
import com.continental.insurance.repository.PaymentRepository;
import com.continental.insurance.service.ReportGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for report generation.
 * Exposes the COBOL RPTGEN report as an HTTP endpoint.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReportGenerationService reportGenerationService;

    /**
     * GET /api/reports/summary — generate a claims processing summary report.
     */
    @GetMapping("/summary")
    public ReportGenerationService.SummaryReport getSummaryReport() {
        List<Claim> allClaims = claimRepository.findAll();

        int total = allClaims.size();
        int approved = 0;
        int denied = 0;
        int pending = 0;

        for (Claim claim : allClaims) {
            if (claim.getStatus() == ClaimStatus.APPROVED) {
                approved++;
            } else if (claim.getStatus() == ClaimStatus.DENIED) {
                denied++;
            } else {
                pending++;
            }
        }

        List<Payment> payments = paymentRepository.findAll();
        BigDecimal totalPaid = BigDecimal.ZERO;
        for (Payment payment : payments) {
            totalPaid = totalPaid.add(payment.getPaymentAmount());
        }

        return reportGenerationService.generateSummary(total, approved, denied, pending, totalPaid);
    }
}
