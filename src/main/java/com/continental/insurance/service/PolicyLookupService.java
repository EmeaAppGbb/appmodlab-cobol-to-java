package com.continental.insurance.service;

import com.continental.insurance.exception.PolicyNotFoundException;
import com.continental.insurance.model.Policy;
import com.continental.insurance.repository.PolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Policy lookup service — translated from COBOL POLYLKUP.cbl.
 * <p>
 * The original COBOL program performed a sequential file search through
 * data/policies.dat to locate a matching policy number. This Java version
 * replaces that with Spring Data JPA repository queries.
 */
@Service
public class PolicyLookupService {

    private static final Logger log = LoggerFactory.getLogger(PolicyLookupService.class);

    @Autowired
    private PolicyRepository policyRepository;

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

        Optional<Policy> policy = policyRepository.findById(policyNumber);

        if (!policy.isPresent()) {
            log.warn("Policy not found: {}", policyNumber);
            throw new PolicyNotFoundException(policyNumber);
        }

        log.debug("Policy found: {}", policy.get().getPolicyNumber());
        return policy.get();
    }

    /**
     * Look up a policy, returning an Optional instead of throwing.
     *
     * @param policyNumber the policy number to search for
     * @return an Optional containing the policy, or empty if not found
     */
    public Optional<Policy> findPolicy(String policyNumber) {
        return policyRepository.findById(policyNumber);
    }

    /**
     * Get all policies.
     *
     * @return list of all policies
     */
    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }
}
