package com.hrms.service.impl;

import com.hrms.entity.LeaveApplication;
import com.hrms.enums.LeaveApplicationStatus;
import com.hrms.repository.LeaveApplicationRepository;
import com.hrms.service.LeaveApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for LeaveApplication operations.
 * TODO: Complete implementation with business logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    private final LeaveApplicationRepository leaveApplicationRepository;

    @Override
    public List<LeaveApplication> getLeaveApplications(String tenantId, Long companyId, Long employeeId,
                                                        LeaveApplicationStatus status, LocalDate fromDate, LocalDate toDate) {
        log.debug("getLeaveApplications - tenantId: {}, companyId: {}, employeeId: {}, status: {}",
                  tenantId, companyId, employeeId, status);

        if (employeeId != null && status != null) {
            return leaveApplicationRepository.findByEmployeeAndStatus(tenantId, companyId, employeeId, status);
        } else if (employeeId != null) {
            return leaveApplicationRepository.findByEmployee(tenantId, companyId, employeeId);
        } else if (fromDate != null && toDate != null) {
            return leaveApplicationRepository.findByCompanyAndDateRange(tenantId, companyId, fromDate, toDate);
        } else if (status != null) {
            return leaveApplicationRepository.findByCompanyAndStatus(tenantId, companyId, status);
        }

        // Default: return first page of results
        return leaveApplicationRepository.findByCompany(tenantId, companyId,
                org.springframework.data.domain.PageRequest.of(0, 100)).getContent();
    }

    @Override
    public Optional<LeaveApplication> getLeaveApplicationById(Long id) {
        log.debug("getLeaveApplicationById - id: {}", id);
        return leaveApplicationRepository.findByIdWithDetails(id);
    }

    @Override
    public Optional<LeaveApplication> getLeaveApplicationByNumber(String tenantId, Long companyId, String applicationNumber) {
        log.debug("getLeaveApplicationByNumber - applicationNumber: {}", applicationNumber);
        return leaveApplicationRepository.findByApplicationNumber(tenantId, companyId, applicationNumber);
    }

    @Override
    public List<LeaveApplication> getPendingApprovals(String tenantId, Long companyId, Long approverId) {
        log.debug("getPendingApprovals - tenantId: {}, companyId: {}, approverId: {}", tenantId, companyId, approverId);
        return leaveApplicationRepository.findPendingForApprover(tenantId, companyId, approverId);
    }

    @Override
    public List<LeaveApplication> getMyApplications(String tenantId, Long companyId, Long employeeId) {
        log.debug("getMyApplications - employeeId: {}", employeeId);
        return leaveApplicationRepository.findByEmployee(tenantId, companyId, employeeId);
    }

    @Override
    @Transactional
    public LeaveApplication createLeaveApplication(String tenantId, Long companyId, Long employeeId, LeaveApplication application) {
        log.debug("createLeaveApplication - tenantId: {}, companyId: {}, employeeId: {}", tenantId, companyId, employeeId);

        application.setTenantId(tenantId);
        application.setStatus(LeaveApplicationStatus.DRAFT);

        // Generate application number
        String prefix = String.format("LA-%d-%s-", companyId, LocalDate.now().getYear());
        Long sequence = leaveApplicationRepository.getNextSequence(tenantId, companyId, prefix);
        application.setApplicationNumber(prefix + String.format("%05d", sequence));

        // TODO: Set employee, company, leave type references
        // TODO: Validate dates and overlaps
        // TODO: Check leave balance

        return leaveApplicationRepository.save(application);
    }

    @Override
    @Transactional
    public LeaveApplication updateLeaveApplication(Long id, LeaveApplication application) {
        log.debug("updateLeaveApplication - id: {}", id);

        LeaveApplication existing = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveApplication not found: " + id));

        if (existing.getStatus() != LeaveApplicationStatus.DRAFT) {
            throw new RuntimeException("Can only update applications in DRAFT status");
        }

        existing.setFromDate(application.getFromDate());
        existing.setToDate(application.getToDate());
        existing.setFromDayType(application.getFromDayType());
        existing.setToDayType(application.getToDayType());
        existing.setReason(application.getReason());
        existing.calculateTotalDays();

        return leaveApplicationRepository.save(existing);
    }

    @Override
    @Transactional
    public LeaveApplication submitApplication(Long id) {
        log.debug("submitApplication - id: {}", id);

        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveApplication not found: " + id));

        if (application.getStatus() != LeaveApplicationStatus.DRAFT) {
            throw new RuntimeException("Can only submit applications in DRAFT status");
        }

        // TODO: Validate leave balance
        // TODO: Check for overlapping applications

        application.setStatus(LeaveApplicationStatus.PENDING);
        application.setAppliedAt(OffsetDateTime.now());

        return leaveApplicationRepository.save(application);
    }

    @Override
    @Transactional
    public LeaveApplication approveApplication(Long id, String remarks) {
        log.debug("approveApplication - id: {}, remarks: {}", id, remarks);

        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveApplication not found: " + id));

        if (application.getStatus() != LeaveApplicationStatus.PENDING) {
            throw new RuntimeException("Can only approve applications in PENDING status");
        }

        application.setStatus(LeaveApplicationStatus.APPROVED);
        // Note: approvalRemarks field not present - storing in reason if needed
        application.setApprovedAt(OffsetDateTime.now());
        // TODO: Set approvedBy from current user

        // TODO: Deduct from leave balance

        return leaveApplicationRepository.save(application);
    }

    @Override
    @Transactional
    public LeaveApplication rejectApplication(Long id, String reason) {
        log.debug("rejectApplication - id: {}, reason: {}", id, reason);

        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveApplication not found: " + id));

        if (application.getStatus() != LeaveApplicationStatus.PENDING) {
            throw new RuntimeException("Can only reject applications in PENDING status");
        }

        application.setStatus(LeaveApplicationStatus.REJECTED);
        application.setRejectionReason(reason);
        application.setApprovedAt(OffsetDateTime.now());
        // TODO: Set approvedBy from current user

        return leaveApplicationRepository.save(application);
    }

    @Override
    @Transactional
    public LeaveApplication cancelApplication(Long id, String reason) {
        log.debug("cancelApplication - id: {}, reason: {}", id, reason);

        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveApplication not found: " + id));

        if (application.getStatus() != LeaveApplicationStatus.APPROVED) {
            throw new RuntimeException("Can only cancel applications in APPROVED status");
        }

        application.setStatus(LeaveApplicationStatus.CANCELLED);
        application.setCancellationReason(reason);
        application.setCancelledAt(OffsetDateTime.now());
        // TODO: Set cancelledBy from current user

        // TODO: Restore leave balance

        return leaveApplicationRepository.save(application);
    }

    @Override
    @Transactional
    public LeaveApplication withdrawApplication(Long id) {
        log.debug("withdrawApplication - id: {}", id);

        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveApplication not found: " + id));

        if (application.getStatus() != LeaveApplicationStatus.PENDING) {
            throw new RuntimeException("Can only withdraw applications in PENDING status");
        }

        application.setStatus(LeaveApplicationStatus.WITHDRAWN);

        return leaveApplicationRepository.save(application);
    }

    @Override
    @Transactional
    public void deleteLeaveApplication(Long id) {
        log.debug("deleteLeaveApplication - id: {}", id);

        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveApplication not found: " + id));

        if (application.getStatus() != LeaveApplicationStatus.DRAFT) {
            // Soft delete
            application.setIsDeleted(true);
            leaveApplicationRepository.save(application);
        } else {
            // Hard delete for drafts
            leaveApplicationRepository.deleteById(id);
        }
    }
}
