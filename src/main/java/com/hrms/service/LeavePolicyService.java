package com.hrms.service;

import com.hrms.entity.LeavePolicy;
import com.hrms.entity.LeavePolicyEntitlement;
import com.hrms.entity.LeavePolicyCriteria;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for LeavePolicy operations.
 */
public interface LeavePolicyService {

    List<LeavePolicy> getLeavePolicies(String tenantId, Long companyId, String status);

    Optional<LeavePolicy> getLeavePolicyById(Long id);

    Optional<LeavePolicy> getLeavePolicyWithEntitlements(Long id);

    Optional<LeavePolicy> getDefaultLeavePolicy(String tenantId, Long companyId);

    LeavePolicy createLeavePolicy(String tenantId, Long companyId, LeavePolicy policy);

    LeavePolicy updateLeavePolicy(Long id, LeavePolicy policy);

    void deleteLeavePolicy(Long id);

    LeavePolicy activatePolicy(Long id);

    LeavePolicy deactivatePolicy(Long id);

    LeavePolicy setDefaultPolicy(Long id);

    LeavePolicy saveEntitlements(Long policyId, List<LeavePolicyEntitlement> entitlements);

    LeavePolicy saveCriteria(Long policyId, List<LeavePolicyCriteria> criteria);

    Long countEmployeesByPolicy(Long policyId);
}
