package com.continental.insurance.service;

import com.continental.insurance.exception.PolicyNotFoundException;
import com.continental.insurance.model.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

/**
 * Policy lookup service — translated from COBOL POLYLKUP.cbl.
 * <p>
 * The original COBOL program performed a sequential file search through
 * data/policies.dat to locate a matching policy number. This Java version
 * replaces that with JPA-based database queries.
 */
@Service
public class PolicyLookupService {

    private static final Logger log = LoggerFactory.getLogger(PolicyLookupService.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Look up a policy by its policy number.
     * Translates COBOL 2000-SEARCH-POLICY / 2200-CHECK-MATCH logic.
     *
     * @param policyNumber the policy number to search for
     * @return the matching Policy
     * @throws PolicyNotFoundException if no policy matches the given number
     */
    public Policy lookupPolicy(String policyNumber) {
        log.debug("Looking up policy: {}", policyNumber);

        Policy policy = entityManager.find(Policy.class, policyNumber);

        if (policy == null) {
            log.warn("Policy not found: {}", policyNumber);
            throw new PolicyNotFoundException(policyNumber);
        }

        log.debug("Policy found: {}", policy.getPolicyNumber());
        return policy;
    }

    /**
     * Look up a policy, returning an Optional instead of throwing.
     *
     * @param policyNumber the policy number to search for
     * @return an Optional containing the policy, or empty if not found
     */
    public Optional<Policy> findPolicy(String policyNumber) {
        return Optional.ofNullable(entityManager.find(Policy.class, policyNumber));
    }
}
