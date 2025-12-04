package com.hrms.service.impl;

import com.hrms.entity.LeavePolicy;
import com.hrms.entity.LeavePolicyCriteria;
import com.hrms.entity.LeavePolicyEntitlement;
import com.hrms.repository.LeavePolicyCriteriaRepository;
import com.hrms.repository.LeavePolicyEntitlementRepository;
import com.hrms.repository.LeavePolicyRepository;
import com.hrms.service.LeavePolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for LeavePolicy operations.
 * TODO: Complete implementation with business logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeavePolicyServiceImpl implements LeavePolicyService {

    private final LeavePolicyRepository leavePolicyRepository;
    private final LeavePolicyEntitlementRepository entitlementRepository;
    private final LeavePolicyCriteriaRepository criteriaRepository;

    @Override
    public List<LeavePolicy> getLeavePolicies(String tenantId, Long companyId, String status) {
        log.debug("getLeavePolicies - tenantId: {}, companyId: {}, status: {}", tenantId, companyId, status);
        if (status != null && !status.isEmpty()) {
            return leavePolicyRepository.findByTenantAndCompanyAndStatus(tenantId, companyId, status);
        }
        return leavePolicyRepository.findByTenantAndCompany(tenantId, companyId);
    }

    @Override
    public Optional<LeavePolicy> getLeavePolicyById(Long id) {
        log.debug("getLeavePolicyById - id: {}", id);
        return leavePolicyRepository.findById(id);
    }

    @Override
    public Optional<LeavePolicy> getLeavePolicyWithEntitlements(Long id) {
        log.debug("getLeavePolicyWithEntitlements - id: {}", id);
        return leavePolicyRepository.findByIdWithEntitlements(id);
    }

    @Override
    public Optional<LeavePolicy> getDefaultLeavePolicy(String tenantId, Long companyId) {
        log.debug("getDefaultLeavePolicy - tenantId: {}, companyId: {}", tenantId, companyId);
        return leavePolicyRepository.findDefaultByTenantAndCompany(tenantId, companyId);
    }

    @Override
    @Transactional
    public LeavePolicy createLeavePolicy(String tenantId, Long companyId, LeavePolicy policy) {
        log.debug("createLeavePolicy - tenantId: {}, companyId: {}, code: {}", tenantId, companyId, policy.getCode());
        policy.setTenantId(tenantId);
        // TODO: Set company reference
        return leavePolicyRepository.save(policy);
    }

    @Override
    @Transactional
    public LeavePolicy updateLeavePolicy(Long id, LeavePolicy policy) {
        log.debug("updateLeavePolicy - id: {}", id);
        LeavePolicy existing = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeavePolicy not found: " + id));
        existing.setName(policy.getName());
        existing.setDescription(policy.getDescription());
        existing.setLeaveYearStart(policy.getLeaveYearStart());
        return leavePolicyRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteLeavePolicy(Long id) {
        log.debug("deleteLeavePolicy - id: {}", id);
        leavePolicyRepository.deleteById(id);
    }

    @Override
    @Transactional
    public LeavePolicy activatePolicy(Long id) {
        log.debug("activatePolicy - id: {}", id);
        LeavePolicy policy = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeavePolicy not found: " + id));
        policy.setStatus("ACTIVE");
        return leavePolicyRepository.save(policy);
    }

    @Override
    @Transactional
    public LeavePolicy deactivatePolicy(Long id) {
        log.debug("deactivatePolicy - id: {}", id);
        LeavePolicy policy = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeavePolicy not found: " + id));
        policy.setStatus("INACTIVE");
        return leavePolicyRepository.save(policy);
    }

    @Override
    @Transactional
    public LeavePolicy setDefaultPolicy(Long id) {
        log.debug("setDefaultPolicy - id: {}", id);
        LeavePolicy policy = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeavePolicy not found: " + id));

        // Clear existing defaults for tenant+company
        leavePolicyRepository.clearDefaultsForTenantAndCompany(
                policy.getTenantId(),
                policy.getCompany() != null ? policy.getCompany().getId() : null);

        policy.setIsDefault(true);
        return leavePolicyRepository.save(policy);
    }

    @Override
    @Transactional
    public LeavePolicy saveEntitlements(Long policyId, List<LeavePolicyEntitlement> entitlements) {
        log.debug("saveEntitlements - policyId: {}, count: {}", policyId, entitlements.size());
        LeavePolicy policy = leavePolicyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("LeavePolicy not found: " + policyId));

        // Delete existing entitlements
        entitlementRepository.deleteByLeavePolicyId(policyId);

        // Save new entitlements
        for (LeavePolicyEntitlement entitlement : entitlements) {
            entitlement.setLeavePolicy(policy);
            entitlementRepository.save(entitlement);
        }

        return leavePolicyRepository.findByIdWithEntitlements(policyId).orElse(policy);
    }

    @Override
    @Transactional
    public LeavePolicy saveCriteria(Long policyId, List<LeavePolicyCriteria> criteria) {
        log.debug("saveCriteria - policyId: {}, count: {}", policyId, criteria.size());
        LeavePolicy policy = leavePolicyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("LeavePolicy not found: " + policyId));

        // Delete existing criteria
        criteriaRepository.deleteByLeavePolicyId(policyId);

        // Save new criteria
        for (LeavePolicyCriteria criterion : criteria) {
            criterion.setLeavePolicy(policy);
            criteriaRepository.save(criterion);
        }

        return leavePolicyRepository.findById(policyId).orElse(policy);
    }

    @Override
    public Long countEmployeesByPolicy(Long policyId) {
        log.debug("countEmployeesByPolicy - policyId: {}", policyId);
        return leavePolicyRepository.countEmployeesByPolicy(policyId);
    }
}
