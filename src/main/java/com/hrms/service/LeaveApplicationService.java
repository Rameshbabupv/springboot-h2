package com.hrms.service;

import com.hrms.entity.LeaveApplication;
import com.hrms.enums.LeaveApplicationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for LeaveApplication operations.
 */
public interface LeaveApplicationService {

    List<LeaveApplication> getLeaveApplications(String tenantId, Long companyId, Long employeeId,
                                                 LeaveApplicationStatus status, LocalDate fromDate, LocalDate toDate);

    Optional<LeaveApplication> getLeaveApplicationById(Long id);

    Optional<LeaveApplication> getLeaveApplicationByNumber(String tenantId, Long companyId, String applicationNumber);

    List<LeaveApplication> getPendingApprovals(String tenantId, Long companyId, Long approverId);

    List<LeaveApplication> getMyApplications(String tenantId, Long companyId, Long employeeId);

    LeaveApplication createLeaveApplication(String tenantId, Long companyId, Long employeeId, LeaveApplication application);

    LeaveApplication updateLeaveApplication(Long id, LeaveApplication application);

    LeaveApplication submitApplication(Long id);

    LeaveApplication approveApplication(Long id, String remarks);

    LeaveApplication rejectApplication(Long id, String reason);

    LeaveApplication cancelApplication(Long id, String reason);

    LeaveApplication withdrawApplication(Long id);

    void deleteLeaveApplication(Long id);
}
