package com.continental.insurance.model;

/**
 * Insurance plan types mapped from COBOL POL-PLAN-TYPE codes.
 * BS=BASIC, SV=SILVER, PR=PREMIUM, BR=BRONZE.
 */
public enum PlanType {

    BASIC("BS"),
    SILVER("SV"),
    PREMIUM("PR"),
    BRONZE("BR");

    private final String code;

    PlanType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Resolve a PlanType from its COBOL two-character code.
     *
     * @param code the COBOL code (e.g. "BS")
     * @return the matching PlanType
     * @throws IllegalArgumentException if the code is unknown
     */
    public static PlanType fromCode(String code) {
        for (PlanType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown plan type code: " + code);
    }
}
