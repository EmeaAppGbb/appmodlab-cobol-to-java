package com.continental.insurance.controller;

import com.continental.insurance.model.Policy;
import com.continental.insurance.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for policy operations.
 * Exposes policy lookup functionality as HTTP endpoints.
 */
@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    @Autowired
    private PolicyRepository policyRepository;

    /**
     * GET /api/policies — list all policies.
     */
    @GetMapping
    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    /**
     * GET /api/policies/{id} — get a single policy by policy number.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Policy> getPolicyById(@PathVariable String id) {
        return policyRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
