package com.continental.insurance.exception;

/**
 * Exception thrown when a policy cannot be found for a claim.
 * Maps to COBOL error code 20 (ERR-POLICY-NOT-FOUND).
 */
public class PolicyNotFoundException extends ClaimsProcessingException {

    public static final int ERR_POLICY_NOT_FOUND = 20;

    private final String policyNumber;

    public PolicyNotFoundException(String policyNumber) {
        super(ERR_POLICY_NOT_FOUND, "Policy not found: " + policyNumber);
        this.policyNumber = policyNumber;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }
}
