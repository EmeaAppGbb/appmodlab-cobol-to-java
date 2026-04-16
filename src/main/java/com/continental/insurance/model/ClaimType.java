package com.continental.insurance.model;

/**
 * Claim types mapped from COBOL CLM-CLAIM-TYPE codes.
 * 01=MEDICAL, 02=DENTAL, 03=VISION, 04=PHARMACY.
 */
public enum ClaimType {

    MEDICAL("01"),
    DENTAL("02"),
    VISION("03"),
    PHARMACY("04");

    private final String code;

    ClaimType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Resolve a ClaimType from its COBOL two-character code.
     *
     * @param code the COBOL code (e.g. "01")
     * @return the matching ClaimType
     * @throws IllegalArgumentException if the code is unknown
     */
    public static ClaimType fromCode(String code) {
        for (ClaimType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown claim type code: " + code);
    }
}
