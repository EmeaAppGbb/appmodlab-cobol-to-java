package com.continental.insurance.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * JPA entity representing an insurance claim.
 * Mapped from COBOL copybook CLMREC.cpy.
 */
@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @Column(name = "claim_number", length = 10)
    private String claimNumber;

    @Column(name = "policy_number", length = 10, nullable = false)
    private String policyNumber;

    @Column(name = "claim_date", nullable = false)
    private LocalDate claimDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", length = 10, nullable = false)
    private ClaimType claimType;

    @Column(name = "claim_amount", precision = 9, scale = 2, nullable = false)
    private BigDecimal claimAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private ClaimStatus status;

    @Column(name = "diagnosis_code", length = 5)
    private String diagnosisCode;

    @Column(name = "provider_id", length = 8)
    private String providerId;

    public Claim() {
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public LocalDate getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
    }

    public ClaimType getClaimType() {
        return claimType;
    }

    public void setClaimType(ClaimType claimType) {
        this.claimType = claimType;
    }

    public BigDecimal getClaimAmount() {
        return claimAmount;
    }

    public void setClaimAmount(BigDecimal claimAmount) {
        this.claimAmount = claimAmount;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getDiagnosisCode() {
        return diagnosisCode;
    }

    public void setDiagnosisCode(String diagnosisCode) {
        this.diagnosisCode = diagnosisCode;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Claim claim = (Claim) o;
        return Objects.equals(claimNumber, claim.claimNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claimNumber);
    }

    @Override
    public String toString() {
        return "Claim{" +
                "claimNumber='" + claimNumber + '\'' +
                ", policyNumber='" + policyNumber + '\'' +
                ", claimType=" + claimType +
                ", claimAmount=" + claimAmount +
                ", status=" + status +
                '}';
    }
}
