package com.hrms.service;

import com.hrms.entity.PayheadMaster;
import com.hrms.enums.PayheadType;
import com.hrms.graphql.input.PayheadInput;

import java.util.List;

/**
 * Service interface for Payhead Master operations
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
public interface PayheadMasterService {

    /**
     * Get all payheads for tenant/company with optional filtering
     */
    List<PayheadMaster> getPayheads(String tenantId, Long companyId, PayheadType type, Boolean isActive);

    /**
     * Get payhead by ID with tenant validation
     */
    PayheadMaster getPayheadById(String tenantId, Long id);

    /**
     * Create new payhead
     */
    PayheadMaster createPayhead(String tenantId, Long companyId, PayheadInput input);

    /**
     * Update existing payhead
     */
    PayheadMaster updatePayhead(String tenantId, Long id, PayheadInput input);

    /**
     * Delete payhead (soft delete if used, hard delete otherwise)
     */
    boolean deletePayhead(String tenantId, Long id);

    /**
     * Get mandatory payheads for validation
     */
    List<PayheadMaster> getMandatoryPayheads(String tenantId, Long companyId);

    /**
     * Check if payhead code exists
     */
    boolean payheadCodeExists(String tenantId, Long companyId, String code);
}
