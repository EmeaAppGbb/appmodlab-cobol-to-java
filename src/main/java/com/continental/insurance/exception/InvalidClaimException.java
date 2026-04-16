package com.continental.insurance.exception;

/**
 * Exception for invalid claim data.
 * Maps to COBOL error code 30 (ERR-INVALID-CLAIM).
 */
public class InvalidClaimException extends ClaimsProcessingException {

    public static final int ERR_INVALID_CLAIM = 30;

    public InvalidClaimException(String message) {
        super(ERR_INVALID_CLAIM, message);
    }
}
