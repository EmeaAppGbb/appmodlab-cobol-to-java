package com.continental.insurance.repository;

import com.continental.insurance.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Policy} entities.
 * Replaces the COBOL sequential file search in POLYLKUP.cbl.
 */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, String> {

    List<Policy> findByPolicyNumber(String policyNumber);

    List<Policy> findByPolicyStatus(String status);
}
