package com.hrms.service;

import com.hrms.dto.response.DeleteLeaveTypeResponse;
import com.hrms.entity.LeaveType;
import com.hrms.graphql.input.LeaveTypeInput;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for LeaveType operations.
 */
public interface LeaveTypeService {

    /**
     * Get leave types for a company with optional isActive filter.
     */
    List<LeaveType> getLeaveTypes(String tenantId, Long companyId, Boolean isActive);

    /**
     * Get single leave type by ID and tenant/company validation.
     */
    Optional<LeaveType> getLeaveTypeById(String tenantId, Long companyId, Long id);

    /**
     * Get leave type by code.
     */
    Optional<LeaveType> getLeaveTypeByCode(String tenantId, Long companyId, String code);

    /**
     * Create new leave type with validation.
     */
    LeaveType createLeaveType(String tenantId, Long companyId, LeaveTypeInput input);

    /**
     * Update existing leave type with validation.
     */
    LeaveType updateLeaveType(String tenantId, Long companyId, Long id, LeaveTypeInput input);

    /**
     * Delete leave type (soft or hard delete based on references).
     */
    DeleteLeaveTypeResponse deleteLeaveType(String tenantId, Long companyId, Long id);
}
