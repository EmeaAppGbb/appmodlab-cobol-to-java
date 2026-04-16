package com.continental.insurance.exception;

/**
 * Exception for file I/O errors.
 * Maps to COBOL error codes 10 (ERR-FILE-OPEN) and 11 (ERR-FILE-READ).
 */
public class FileProcessingException extends ClaimsProcessingException {

    public static final int ERR_FILE_OPEN = 10;
    public static final int ERR_FILE_READ = 11;

    public FileProcessingException(int errorCode, String message) {
        super(errorCode, message);
    }

    public FileProcessingException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
