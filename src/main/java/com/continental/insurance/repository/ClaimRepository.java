package com.continental.insurance.repository;

import com.continental.insurance.model.Claim;
import com.continental.insurance.model.ClaimStatus;
import com.continental.insurance.model.ClaimType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Claim} entities.
 * Replaces the COBOL sequential file I/O in CLMPROC.cbl.
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, String> {

    List<Claim> findByPolicyNumber(String policyNumber);

    List<Claim> findByStatus(ClaimStatus status);

    List<Claim> findByClaimType(ClaimType claimType);
}
