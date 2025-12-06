package com.hrms.service.impl;

import com.hrms.entity.LeaveType;
import com.hrms.repository.LeaveTypeRepository;
import com.hrms.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for LeaveType operations.
 * TODO: Complete implementation with business logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    @Override
    public List<LeaveType> getLeaveTypes(String tenantId, Long companyId, Boolean activeOnly) {
        log.debug("getLeaveTypes - tenantId: {}, companyId: {}, activeOnly: {}", tenantId, companyId, activeOnly);
        if (activeOnly != null && activeOnly) {
            return leaveTypeRepository.findActiveByTenantAndCompany(tenantId, companyId);
        }
        return leaveTypeRepository.findByTenantAndCompany(tenantId, companyId);
    }

    @Override
    public Optional<LeaveType> getLeaveTypeById(Long id) {
        log.debug("getLeaveTypeById - id: {}", id);
        return leaveTypeRepository.findById(id);
    }

    @Override
    public Optional<LeaveType> getLeaveTypeByCode(String tenantId, Long companyId, String code) {
        log.debug("getLeaveTypeByCode - tenantId: {}, companyId: {}, code: {}", tenantId, companyId, code);
        return leaveTypeRepository.findByTenantAndCompanyAndCode(tenantId, companyId, code);
    }

    @Override
    public List<LeaveType> getSharedLeaveTypes(String tenantId) {
        log.debug("getSharedLeaveTypes - tenantId: {}", tenantId);
        return leaveTypeRepository.findSharedByTenant(tenantId);
    }

    @Override
    @Transactional
    public LeaveType createLeaveType(String tenantId, Long companyId, LeaveType leaveType) {
        log.debug("createLeaveType - tenantId: {}, companyId: {}, code: {}", tenantId, companyId, leaveType.getCode());
        leaveType.setTenantId(tenantId);
        if (companyId != null) {
            // TODO: Set company reference
        }
        return leaveTypeRepository.save(leaveType);
    }

    @Override
    @Transactional
    public LeaveType updateLeaveType(Long id, LeaveType leaveType) {
        log.debug("updateLeaveType - id: {}", id);
        LeaveType existing = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveType not found: " + id));
        existing.setName(leaveType.getName());
        existing.setDescription(leaveType.getDescription());
        existing.setCategory(leaveType.getCategory());
        existing.setColor(leaveType.getColor());
        return leaveTypeRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteLeaveType(Long id) {
        log.debug("deleteLeaveType - id: {}", id);
        leaveTypeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public LeaveType toggleStatus(Long id) {
        log.debug("toggleStatus - id: {}", id);
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveType not found: " + id));
        leaveType.setIsActive(!leaveType.getIsActive());
        return leaveTypeRepository.save(leaveType);
    }
}
