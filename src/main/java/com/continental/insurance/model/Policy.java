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
 * JPA entity representing an insurance policy.
 * Mapped from COBOL copybook POLREC.cpy.
 */
@Entity
@Table(name = "policies")
public class Policy {

    @Id
    @Column(name = "policy_number", length = 10)
    private String policyNumber;

    @Column(name = "holder_name", length = 28, nullable = false)
    private String holderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", length = 10, nullable = false)
    private PlanType planType;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "deductible", precision = 7, scale = 2, nullable = false)
    private BigDecimal deductible;

    @Column(name = "max_coverage", precision = 9, scale = 2, nullable = false)
    private BigDecimal maxCoverage;

    @Column(name = "status", length = 10, nullable = false)
    private String policyStatus;

    public Policy() {
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BigDecimal getDeductible() {
        return deductible;
    }

    public void setDeductible(BigDecimal deductible) {
        this.deductible = deductible;
    }

    public BigDecimal getMaxCoverage() {
        return maxCoverage;
    }

    public void setMaxCoverage(BigDecimal maxCoverage) {
        this.maxCoverage = maxCoverage;
    }

    public String getPolicyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(String policyStatus) {
        this.policyStatus = policyStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Policy policy = (Policy) o;
        return Objects.equals(policyNumber, policy.policyNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyNumber);
    }

    @Override
    public String toString() {
        return "Policy{" +
                "policyNumber='" + policyNumber + '\'' +
                ", holderName='" + holderName + '\'' +
                ", planType=" + planType +
                ", status='" + policyStatus + '\'' +
                '}';
    }
}
