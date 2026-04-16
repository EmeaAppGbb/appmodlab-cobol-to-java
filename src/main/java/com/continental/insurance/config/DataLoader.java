package com.continental.insurance.config;

import com.continental.insurance.model.Claim;
import com.continental.insurance.model.ClaimStatus;
import com.continental.insurance.model.ClaimType;
import com.continental.insurance.model.PlanType;
import com.continental.insurance.model.Policy;
import com.continental.insurance.repository.ClaimRepository;
import com.continental.insurance.repository.PolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Loads fixed-width COBOL data files (claims.dat, policies.dat) into the
 * H2 database on application startup.
 * <p>
 * Record layouts mirror the original COBOL copybook definitions.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Claims record layout (0-based offsets)
    private static final int CLM_NUMBER_START = 0;
    private static final int CLM_NUMBER_END = 10;
    private static final int CLM_POLICY_START = 10;
    private static final int CLM_POLICY_END = 20;
    private static final int CLM_DATE_START = 20;
    private static final int CLM_DATE_END = 28;
    private static final int CLM_TYPE_START = 28;
    private static final int CLM_TYPE_END = 30;
    private static final int CLM_AMOUNT_START = 30;
    private static final int CLM_AMOUNT_END = 38;
    private static final int CLM_DIAG_START = 38;
    private static final int CLM_DIAG_END = 43;
    private static final int CLM_PROVIDER_START = 45;
    private static final int CLM_PROVIDER_END = 53;

    // Policy record layout (0-based offsets)
    private static final int POL_NUMBER_START = 0;
    private static final int POL_NUMBER_END = 10;
    private static final int POL_HOLDER_START = 10;
    private static final int POL_HOLDER_END = 38;
    private static final int POL_PLAN_START = 38;
    private static final int POL_PLAN_END = 40;
    private static final int POL_EFF_DATE_START = 40;
    private static final int POL_EFF_DATE_END = 48;
    private static final int POL_EXP_DATE_START = 48;
    private static final int POL_EXP_DATE_END = 56;
    private static final int POL_DEDUCTIBLE_START = 56;
    private static final int POL_DEDUCTIBLE_END = 63;
    private static final int POL_MAX_COV_START = 63;
    private static final int POL_MAX_COV_END = 71;
    private static final int POL_STATUS_START = 71;
    private static final int POL_STATUS_END = 72;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Loading COBOL data files into H2 database...");
        loadPolicies();
        loadClaims();
        log.info("Data loading complete. Policies: {}, Claims: {}",
                policyRepository.count(), claimRepository.count());
    }

    private void loadPolicies() throws IOException {
        Path path = Paths.get("data", "policies.dat");
        log.info("Loading policies from {}", path.toAbsolutePath());

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    Policy policy = parsePolicy(line);
                    policyRepository.save(policy);
                } catch (Exception e) {
                    log.warn("Skipping invalid policy at line {}: {}", lineNum, e.getMessage());
                }
            }
        }
    }

    private void loadClaims() throws IOException {
        Path path = Paths.get("data", "claims.dat");
        log.info("Loading claims from {}", path.toAbsolutePath());

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    Claim claim = parseClaim(line);
                    claimRepository.save(claim);
                } catch (Exception e) {
                    log.warn("Skipping invalid claim at line {}: {}", lineNum, e.getMessage());
                }
            }
        }
    }

    /**
     * Parse a fixed-width claim record.
     * <p>
     * Layout (COBOL copybook CLMREC):
     * <pre>
     *   Pos  0-9   PIC X(10)   Claim number
     *   Pos 10-19  PIC X(10)   Policy number
     *   Pos 20-27  PIC 9(8)    Claim date (YYYYMMDD)
     *   Pos 28-29  PIC X(2)    Claim type code
     *   Pos 30-37  PIC 9(6)V99 Claim amount (implied 2 decimal places)
     *   Pos 38-42  PIC X(5)    Diagnosis code
     *   Pos 43-44  FILLER      (2 bytes)
     *   Pos 45-52  PIC X(8)    Provider ID
     * </pre>
     */
    private Claim parseClaim(String line) {
        Claim claim = new Claim();
        claim.setClaimNumber(safeSubstring(line, CLM_NUMBER_START, CLM_NUMBER_END).trim());
        claim.setPolicyNumber(safeSubstring(line, CLM_POLICY_START, CLM_POLICY_END).trim());
        claim.setClaimDate(LocalDate.parse(
                safeSubstring(line, CLM_DATE_START, CLM_DATE_END), DATE_FORMAT));
        claim.setClaimType(ClaimType.fromCode(
                safeSubstring(line, CLM_TYPE_START, CLM_TYPE_END).trim()));

        String amountStr = safeSubstring(line, CLM_AMOUNT_START, CLM_AMOUNT_END).trim();
        claim.setClaimAmount(new BigDecimal(amountStr).movePointLeft(2));

        claim.setDiagnosisCode(safeSubstring(line, CLM_DIAG_START, CLM_DIAG_END).trim());

        String providerId = safeSubstring(line, CLM_PROVIDER_START, CLM_PROVIDER_END).trim();
        claim.setProviderId(providerId.isEmpty() ? null : providerId);

        // All loaded claims start as PENDING — adjudication has not yet run
        claim.setStatus(ClaimStatus.PENDING);

        return claim;
    }

    /**
     * Parse a fixed-width policy record.
     * <p>
     * Layout (COBOL copybook POLREC):
     * <pre>
     *   Pos  0-9   PIC X(10)   Policy number
     *   Pos 10-37  PIC X(28)   Holder name
     *   Pos 38-39  PIC X(2)    Plan type code
     *   Pos 40-47  PIC 9(8)    Effective date (YYYYMMDD)
     *   Pos 48-55  PIC 9(8)    Expiry date (YYYYMMDD)
     *   Pos 56-62  PIC 9(5)V99 Deductible (implied 2 decimal places)
     *   Pos 63-70  PIC 9(6)V99 Max coverage (implied 2 decimal places)
     *   Pos 71     PIC X(1)    Policy status (A=Active, I=Inactive)
     * </pre>
     */
    private Policy parsePolicy(String line) {
        Policy policy = new Policy();
        policy.setPolicyNumber(safeSubstring(line, POL_NUMBER_START, POL_NUMBER_END).trim());
        policy.setHolderName(safeSubstring(line, POL_HOLDER_START, POL_HOLDER_END).trim());
        policy.setPlanType(PlanType.fromCode(
                safeSubstring(line, POL_PLAN_START, POL_PLAN_END).trim()));
        policy.setEffectiveDate(LocalDate.parse(
                safeSubstring(line, POL_EFF_DATE_START, POL_EFF_DATE_END), DATE_FORMAT));
        policy.setExpiryDate(LocalDate.parse(
                safeSubstring(line, POL_EXP_DATE_START, POL_EXP_DATE_END), DATE_FORMAT));

        String deductibleStr = safeSubstring(line, POL_DEDUCTIBLE_START, POL_DEDUCTIBLE_END).trim();
        policy.setDeductible(new BigDecimal(deductibleStr).movePointLeft(2));

        String maxCovStr = safeSubstring(line, POL_MAX_COV_START, POL_MAX_COV_END).trim();
        policy.setMaxCoverage(new BigDecimal(maxCovStr).movePointLeft(2));

        policy.setPolicyStatus(safeSubstring(line, POL_STATUS_START, POL_STATUS_END).trim());

        return policy;
    }

    private static String safeSubstring(String line, int start, int end) {
        if (line.length() < end) {
            return line.substring(start);
        }
        return line.substring(start, end);
    }
}
