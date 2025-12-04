package com.hrms.service.impl;

import com.hrms.entity.LeaveBalance;
import com.hrms.repository.LeaveBalanceRepository;
import com.hrms.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for LeaveBalance operations.
 * TODO: Complete implementation with business logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;

    @Override
    public List<LeaveBalance> getEmployeeBalances(String tenantId, Long companyId, Long employeeId, String leaveYear) {
        log.debug("getEmployeeBalances - tenantId: {}, companyId: {}, employeeId: {}, leaveYear: {}",
                  tenantId, companyId, employeeId, leaveYear);
        return leaveBalanceRepository.findByEmployeeAndYear(tenantId, companyId, employeeId, leaveYear);
    }

    @Override
    public List<LeaveBalance> getBalancesByCompany(String tenantId, Long companyId, String leaveYear) {
        log.debug("getBalancesByCompany - tenantId: {}, companyId: {}, leaveYear: {}",
                  tenantId, companyId, leaveYear);
        return leaveBalanceRepository.findByCompanyAndYear(tenantId, companyId, leaveYear);
    }

    @Override
    public Optional<LeaveBalance> getBalanceById(Long id) {
        log.debug("getBalanceById - id: {}", id);
        return leaveBalanceRepository.findById(id);
    }

    @Override
    @Transactional
    public LeaveBalance adjustBalance(Long id, BigDecimal adjustment, String reason) {
        log.debug("adjustBalance - id: {}, adjustment: {}, reason: {}", id, adjustment, reason);
        LeaveBalance balance = leaveBalanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveBalance not found: " + id));

        BigDecimal currentAdjusted = balance.getAdjusted() != null ? balance.getAdjusted() : BigDecimal.ZERO;
        balance.setAdjusted(currentAdjusted.add(adjustment));
        // Recalculate available: opening + credited - used - pending - lapsed - encashed + carried_forward + adjusted
        recalculateAvailable(balance);

        // TODO: Log the adjustment with reason

        return leaveBalanceRepository.save(balance);
    }

    @Override
    @Transactional
    public LeaveBalance creditBalance(String tenantId, Long employeeId, Long leaveTypeId, String leaveYear, BigDecimal amount) {
        log.debug("creditBalance - tenantId: {}, employeeId: {}, leaveTypeId: {}, leaveYear: {}, amount: {}",
                  tenantId, employeeId, leaveTypeId, leaveYear, amount);

        // Find or create balance record
        Optional<LeaveBalance> existingOpt = leaveBalanceRepository.findByEmployeeAndTypeAndYear(
                tenantId, employeeId, leaveTypeId, leaveYear);

        LeaveBalance balance;
        if (existingOpt.isPresent()) {
            balance = existingOpt.get();
            BigDecimal currentCredited = balance.getCredited() != null ? balance.getCredited() : BigDecimal.ZERO;
            balance.setCredited(currentCredited.add(amount));
        } else {
            // TODO: Create new balance record
            throw new RuntimeException("Balance record not found. Initialize balances first.");
        }

        recalculateAvailable(balance);
        return leaveBalanceRepository.save(balance);
    }

    @Override
    @Transactional
    public List<LeaveBalance> initializeEmployeeBalances(String tenantId, Long companyId, Long employeeId, String leaveYear) {
        log.debug("initializeEmployeeBalances - tenantId: {}, companyId: {}, employeeId: {}, leaveYear: {}",
                  tenantId, companyId, employeeId, leaveYear);

        // TODO: Get employee's leave policy
        // TODO: Create balance records for each leave type in the policy
        // TODO: Credit opening balances based on policy rules

        log.warn("initializeEmployeeBalances - Not yet implemented");
        return Collections.emptyList();
    }

    /**
     * Recalculate the available balance.
     * Formula: opening + credited - used - pending - lapsed - encashed + carried_forward + adjusted
     */
    private void recalculateAvailable(LeaveBalance balance) {
        BigDecimal opening = balance.getOpeningBalance() != null ? balance.getOpeningBalance() : BigDecimal.ZERO;
        BigDecimal credited = balance.getCredited() != null ? balance.getCredited() : BigDecimal.ZERO;
        BigDecimal used = balance.getUsed() != null ? balance.getUsed() : BigDecimal.ZERO;
        BigDecimal pending = balance.getPending() != null ? balance.getPending() : BigDecimal.ZERO;
        BigDecimal lapsed = balance.getLapsed() != null ? balance.getLapsed() : BigDecimal.ZERO;
        BigDecimal encashed = balance.getEncashed() != null ? balance.getEncashed() : BigDecimal.ZERO;
        BigDecimal carriedForward = balance.getCarriedForward() != null ? balance.getCarriedForward() : BigDecimal.ZERO;
        BigDecimal adjusted = balance.getAdjusted() != null ? balance.getAdjusted() : BigDecimal.ZERO;

        BigDecimal available = opening
                .add(credited)
                .subtract(used)
                .subtract(pending)
                .subtract(lapsed)
                .subtract(encashed)
                .add(carriedForward)
                .add(adjusted);

        balance.setAvailable(available);
    }
}
