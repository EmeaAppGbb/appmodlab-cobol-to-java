package com.continental.insurance.service;

import com.continental.insurance.exception.PolicyNotFoundException;
import com.continental.insurance.model.Claim;
import com.continental.insurance.model.ClaimStatus;
import com.continental.insurance.model.ClaimType;
import com.continental.insurance.model.Payment;
import com.continental.insurance.model.PaymentStatus;
import com.continental.insurance.model.PlanType;
import com.continental.insurance.model.Policy;
import com.continental.insurance.repository.ClaimRepository;
import com.continental.insurance.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ClaimsProcessingService — orchestration logic
 * translated from COBOL CLMPROC.cbl.
 */
@ExtendWith(MockitoExtension.class)
class ClaimsProcessingServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PolicyLookupService policyLookupService;

    @Mock
    private AdjudicationService adjudicationService;

    @Mock
    private PaymentAuthorizationService paymentAuthorizationService;

    @Mock
    private ReportGenerationService reportGenerationService;

    @InjectMocks
    private ClaimsProcessingService service;

    private Policy testPolicy;
    private Claim approvedClaim;
    private Claim deniedClaim;
    private Claim pendingClaim;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testPolicy = new Policy();
        testPolicy.setPolicyNumber("POL0000001");
        testPolicy.setHolderName("Test Holder");
        testPolicy.setPlanType(PlanType.BASIC);
        testPolicy.setEffectiveDate(LocalDate.of(2024, 1, 1));
        testPolicy.setExpiryDate(LocalDate.of(2026, 12, 31));
        testPolicy.setDeductible(new BigDecimal("500.00"));
        testPolicy.setMaxCoverage(new BigDecimal("50000.00"));
        testPolicy.setPolicyStatus("A");

        approvedClaim = createClaim("CLM0000001", "POL0000001");
        deniedClaim = createClaim("CLM0000002", "POL0000002");
        pendingClaim = createClaim("CLM0000003", "POL0000001");

        testPayment = new Payment();
        testPayment.setClaimNumber("CLM0000001");
        testPayment.setPaymentAmount(new BigDecimal("500.00"));
        testPayment.setPaymentDate(LocalDate.now());
        testPayment.setAuthCode("A00001");
        testPayment.setStatus(PaymentStatus.AUTHORIZED);
    }

    private Claim createClaim(String claimNumber, String policyNumber) {
        Claim c = new Claim();
        c.setClaimNumber(claimNumber);
        c.setPolicyNumber(policyNumber);
        c.setClaimDate(LocalDate.of(2025, 6, 15));
        c.setClaimType(ClaimType.MEDICAL);
        c.setClaimAmount(new BigDecimal("1000.00"));
        c.setDiagnosisCode("A1234");
        c.setProviderId("PROV0001");
        return c;
    }

    @Test
    @DisplayName("Process single claim with valid policy — approved flow")
    void processSingleClaimApproved() {
        when(policyLookupService.lookupPolicy("POL0000001")).thenReturn(testPolicy);
        when(adjudicationService.adjudicate(approvedClaim, testPolicy)).thenReturn(ClaimStatus.APPROVED);
        when(paymentAuthorizationService.authorizePayment(approvedClaim, testPolicy)).thenReturn(testPayment);

        List<Payment> payments = new ArrayList<>();
        ClaimStatus result = service.processSingleClaim(approvedClaim, payments);

        assertEquals(ClaimStatus.APPROVED, result);
        assertEquals(ClaimStatus.APPROVED, approvedClaim.getStatus());
        assertEquals(1, payments.size());
        assertEquals(new BigDecimal("500.00"), payments.get(0).getPaymentAmount());
        verify(paymentRepository).save(testPayment);
        verify(claimRepository).save(approvedClaim);
    }

    @Test
    @DisplayName("Handle missing policy — claim denied")
    void handleMissingPolicy() {
        when(policyLookupService.lookupPolicy("POL0000002"))
                .thenThrow(new PolicyNotFoundException("POL0000002"));

        List<Payment> payments = new ArrayList<>();
        ClaimStatus result = service.processSingleClaim(deniedClaim, payments);

        assertEquals(ClaimStatus.DENIED, result);
        assertEquals(ClaimStatus.DENIED, deniedClaim.getStatus());
        assertEquals(0, payments.size());
        verify(adjudicationService, never()).adjudicate(any(), any());
        verify(paymentAuthorizationService, never()).authorizePayment(any(), any());
    }

    @Test
    @DisplayName("processAllClaims counts approved, denied, and pending correctly")
    void processAllClaimsCountsCorrectly() {
        when(claimRepository.findAll()).thenReturn(Arrays.asList(approvedClaim, deniedClaim, pendingClaim));

        // Claim 1: approved
        when(policyLookupService.lookupPolicy("POL0000001")).thenReturn(testPolicy);
        when(adjudicationService.adjudicate(approvedClaim, testPolicy)).thenReturn(ClaimStatus.APPROVED);
        when(paymentAuthorizationService.authorizePayment(approvedClaim, testPolicy)).thenReturn(testPayment);

        // Claim 2: policy not found → denied
        when(policyLookupService.lookupPolicy("POL0000002"))
                .thenThrow(new PolicyNotFoundException("POL0000002"));

        // Claim 3: pending
        when(adjudicationService.adjudicate(pendingClaim, testPolicy)).thenReturn(ClaimStatus.PENDING);

        ReportGenerationService.SummaryReport mockReport = new ReportGenerationService.SummaryReport(
                3, 1, 1, 1, new BigDecimal("500.00"),
                33, 33, 33, "06/15/2025", Collections.emptyList());
        when(reportGenerationService.generateSummary(eq(3), eq(1), eq(1), eq(1), any(BigDecimal.class)))
                .thenReturn(mockReport);

        ClaimsProcessingService.ProcessingResult result = service.processAllClaims();

        assertEquals(3, result.getClaimsRead());
        assertEquals(1, result.getClaimsApproved());
        assertEquals(1, result.getClaimsDenied());
        assertEquals(1, result.getClaimsPending());
        assertEquals(1, result.getPayments().size());
    }

    @Test
    @DisplayName("Denied claim does not trigger payment authorization")
    void deniedClaimNoPayment() {
        when(policyLookupService.lookupPolicy("POL0000001")).thenReturn(testPolicy);
        when(adjudicationService.adjudicate(approvedClaim, testPolicy)).thenReturn(ClaimStatus.DENIED);

        List<Payment> payments = new ArrayList<>();
        service.processSingleClaim(approvedClaim, payments);

        assertEquals(0, payments.size());
        verify(paymentAuthorizationService, never()).authorizePayment(any(), any());
    }

    @Test
    @DisplayName("Pending claim does not trigger payment authorization")
    void pendingClaimNoPayment() {
        when(policyLookupService.lookupPolicy("POL0000001")).thenReturn(testPolicy);
        when(adjudicationService.adjudicate(approvedClaim, testPolicy)).thenReturn(ClaimStatus.PENDING);

        List<Payment> payments = new ArrayList<>();
        service.processSingleClaim(approvedClaim, payments);

        assertEquals(0, payments.size());
        verify(paymentAuthorizationService, never()).authorizePayment(any(), any());
    }
}
