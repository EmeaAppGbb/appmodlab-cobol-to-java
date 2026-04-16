package com.continental.insurance.exception;

/**
 * Base exception for claims processing errors.
 * Translated from COBOL ERRHANDL.cbl error handling routines.
 */
public class ClaimsProcessingException extends RuntimeException {

    private final int errorCode;

    public ClaimsProcessingException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ClaimsProcessingException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
