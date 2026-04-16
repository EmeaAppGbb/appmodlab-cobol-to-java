package com.continental.insurance.model;

/**
 * Payment authorization status — mirrors COBOL PYMT-STATUS values.
 * A=AUTHORIZED, D=DENIED, P=PENDING.
 */
public enum PaymentStatus {

    AUTHORIZED("A"),
    DENIED("D"),
    PENDING("P");

    private final String code;

    PaymentStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Resolve a PaymentStatus from its COBOL single-character code.
     *
     * @param code the COBOL code (e.g. "A")
     * @return the matching PaymentStatus
     * @throws IllegalArgumentException if the code is unknown
     */
    public static PaymentStatus fromCode(String code) {
        for (PaymentStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown payment status code: " + code);
    }
}
