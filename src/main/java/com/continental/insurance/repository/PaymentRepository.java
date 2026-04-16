package com.continental.insurance.repository;

import com.continental.insurance.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Payment} entities.
 * Replaces the COBOL payment record output in PYMTAUTH.cbl.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByClaimNumber(String claimNumber);
}
