package com.hrms.service;

import com.hrms.entity.LeaveBalance;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for LeaveBalance operations.
 */
public interface LeaveBalanceService {

    List<LeaveBalance> getEmployeeBalances(String tenantId, Long companyId, Long employeeId, String leaveYear);

    List<LeaveBalance> getBalancesByCompany(String tenantId, Long companyId, String leaveYear);

    Optional<LeaveBalance> getBalanceById(Long id);

    LeaveBalance adjustBalance(Long id, BigDecimal adjustment, String reason);

    LeaveBalance creditBalance(String tenantId, Long employeeId, Long leaveTypeId, String leaveYear, BigDecimal amount);

    List<LeaveBalance> initializeEmployeeBalances(String tenantId, Long companyId, Long employeeId, String leaveYear);
}
