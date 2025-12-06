package com.hrms.service;

import com.hrms.entity.LeaveType;
import com.hrms.enums.LeaveCategory;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for LeaveType operations.
 */
public interface LeaveTypeService {

    List<LeaveType> getLeaveTypes(String tenantId, Long companyId, Boolean activeOnly);

    Optional<LeaveType> getLeaveTypeById(Long id);

    Optional<LeaveType> getLeaveTypeByCode(String tenantId, Long companyId, String code);

    List<LeaveType> getSharedLeaveTypes(String tenantId);

    LeaveType createLeaveType(String tenantId, Long companyId, LeaveType leaveType);

    LeaveType updateLeaveType(Long id, LeaveType leaveType);

    void deleteLeaveType(Long id);

    LeaveType toggleStatus(Long id);
}
