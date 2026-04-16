package com.continental.insurance.exception;

/**
 * Exception for arithmetic overflow during payment calculations.
 * Maps to COBOL error code 40 (ERR-CALC-OVERFLOW).
 */
public class CalculationOverflowException extends ClaimsProcessingException {

    public static final int ERR_CALC_OVERFLOW = 40;

    public CalculationOverflowException(String message) {
        super(ERR_CALC_OVERFLOW, message);
    }
}
