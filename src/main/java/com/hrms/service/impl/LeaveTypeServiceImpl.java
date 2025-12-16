package com.hrms.service.impl;

import com.hrms.dto.response.DeleteLeaveTypeResponse;
import com.hrms.entity.Company;
import com.hrms.entity.LeaveType;
import com.hrms.graphql.input.LeaveTypeInput;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.LeaveTypeRepository;
import com.hrms.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service implementation for LeaveType operations with comprehensive validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final CompanyRepository companyRepository;

    // Validation patterns
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private static final int MAX_CODE_LENGTH = 10;
    private static final int MAX_NAME_LENGTH = 100;

    @Override
    public List<LeaveType> getLeaveTypes(String tenantId, Long companyId, Boolean isActive) {
        log.debug("getLeaveTypes - tenantId: {}, companyId: {}, isActive: {}", tenantId, companyId, isActive);
        return leaveTypeRepository.findByTenantAndCompanyWithFilter(tenantId, companyId, isActive);
    }

    @Override
    public Optional<LeaveType> getLeaveTypeById(String tenantId, Long companyId, Long id) {
        log.debug("getLeaveTypeById - tenantId: {}, companyId: {}, id: {}", tenantId, companyId, id);
        return leaveTypeRepository.findById(id)
                .filter(lt -> lt.getTenantId().equals(tenantId))
                .filter(lt -> lt.getCompany() == null || lt.getCompany().getId().equals(companyId));
    }

    @Override
    public Optional<LeaveType> getLeaveTypeByCode(String tenantId, Long companyId, String code) {
        log.debug("getLeaveTypeByCode - tenantId: {}, companyId: {}, code: {}", tenantId, companyId, code);
        return leaveTypeRepository.findByTenantAndCompanyAndCode(tenantId, companyId, code.toUpperCase());
    }

    @Override
    @Transactional
    public LeaveType createLeaveType(String tenantId, Long companyId, LeaveTypeInput input) {
        log.debug("createLeaveType - tenantId: {}, companyId: {}, code: {}", tenantId, companyId, input.getCode());

        // Validation 1: Required fields
        validateRequiredFields(input);

        // Validation 2: Code format and length
        String code = validateAndNormalizeCode(input.getCode());

        // Validation 3: Name length
        validateNameLength(input.getName());

        // Validation 4: Color code format (if provided)
        if (input.getColorCode() != null && !input.getColorCode().isBlank()) {
            validateColorCode(input.getColorCode());
        }

        // Validation 5: Display order (if provided)
        if (input.getDisplayOrder() != null && input.getDisplayOrder() < 0) {
            throw new IllegalArgumentException("Display order must be a positive integer");
        }

        // Validation 6: Check duplicate code
        if (leaveTypeRepository.existsByTenantAndCompanyAndCode(tenantId, companyId, code)) {
            throw new IllegalArgumentException("DUPLICATE_CODE - Leave type with code '" + code + "' already exists");
        }

        // Create entity
        LeaveType leaveType = new LeaveType();
        leaveType.setTenantId(tenantId);

        // Set company reference if companyId is provided
        if (companyId != null) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
            leaveType.setCompany(company);
        }

        leaveType.setCode(code);
        leaveType.setName(input.getName().trim());
        leaveType.setCategory(input.getCategory());
        leaveType.setDescription(input.getDescription() != null ? input.getDescription().trim() : null);
        leaveType.setIcon(input.getIcon());
        leaveType.setColorCode(input.getColorCode());
        leaveType.setDisplayOrder(input.getDisplayOrder());
        leaveType.setIsActive(input.getIsActive() != null ? input.getIsActive() : true);

        // TODO: Set createdBy from security context
        leaveType.setCreatedBy("SYSTEM");
        leaveType.setUpdatedBy("SYSTEM");

        LeaveType saved = leaveTypeRepository.save(leaveType);
        log.info("Created leave type: {} for tenant: {}, company: {}", code, tenantId, companyId);
        return saved;
    }

    @Override
    @Transactional
    public LeaveType updateLeaveType(String tenantId, Long companyId, Long id, LeaveTypeInput input) {
        log.debug("updateLeaveType - tenantId: {}, companyId: {}, id: {}", tenantId, companyId, id);

        // Find existing leave type with tenant/company validation
        LeaveType existing = leaveTypeRepository.findById(id)
                .filter(lt -> lt.getTenantId().equals(tenantId))
                .filter(lt -> lt.getCompany() == null || lt.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND - Leave type not found: " + id));

        // Validation 1: Required fields (if provided)
        if (input.getName() != null && input.getName().isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        // Validation 2: Code validation (if changed)
        if (input.getCode() != null && !input.getCode().isBlank()) {
            String code = validateAndNormalizeCode(input.getCode());

            // Check if code is different and not duplicate
            if (!code.equals(existing.getCode())) {
                if (leaveTypeRepository.existsByTenantAndCompanyAndCodeExcludingId(
                        tenantId, companyId, code, id)) {
                    throw new IllegalArgumentException("DUPLICATE_CODE - Leave type with code '" + code + "' already exists");
                }
                existing.setCode(code);
            }
        }

        // Validation 3: Name length
        if (input.getName() != null) {
            validateNameLength(input.getName());
            existing.setName(input.getName().trim());
        }

        // Validation 4: Color code format
        if (input.getColorCode() != null) {
            if (!input.getColorCode().isBlank()) {
                validateColorCode(input.getColorCode());
            }
            existing.setColorCode(input.getColorCode());
        }

        // Validation 5: Display order
        if (input.getDisplayOrder() != null) {
            if (input.getDisplayOrder() < 0) {
                throw new IllegalArgumentException("Display order must be a positive integer");
            }
            existing.setDisplayOrder(input.getDisplayOrder());
        }

        // Update other fields
        if (input.getCategory() != null) {
            existing.setCategory(input.getCategory());
        }
        if (input.getDescription() != null) {
            existing.setDescription(input.getDescription().trim());
        }
        if (input.getIcon() != null) {
            existing.setIcon(input.getIcon());
        }
        if (input.getIsActive() != null) {
            existing.setIsActive(input.getIsActive());
        }

        // TODO: Set updatedBy from security context
        existing.setUpdatedBy("SYSTEM");

        LeaveType updated = leaveTypeRepository.save(existing);
        log.info("Updated leave type: {} for tenant: {}, company: {}", updated.getCode(), tenantId, companyId);
        return updated;
    }

    @Override
    @Transactional
    public DeleteLeaveTypeResponse deleteLeaveType(String tenantId, Long companyId, Long id) {
        log.debug("deleteLeaveType - tenantId: {}, companyId: {}, id: {}", tenantId, companyId, id);

        // Find existing leave type with tenant/company validation
        LeaveType existing = leaveTypeRepository.findById(id)
                .filter(lt -> lt.getTenantId().equals(tenantId))
                .filter(lt -> lt.getCompany() == null || lt.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND - Leave type not found: " + id));

        // Check if leave type is referenced
        boolean referencedInPolicies = leaveTypeRepository.isReferencedInLeavePolicies(id);
        boolean referencedInApplications = leaveTypeRepository.isReferencedInLeaveApplications(id);
        boolean referencedInBalances = leaveTypeRepository.isReferencedInLeaveBalances(id);

        if (referencedInPolicies || referencedInApplications || referencedInBalances) {
            // Soft delete - set isActive = false
            existing.setIsActive(false);
            existing.setUpdatedBy("SYSTEM");
            leaveTypeRepository.save(existing);

            int refCount = 0;
            StringBuilder reason = new StringBuilder("IN_USE - Referenced in: ");
            if (referencedInPolicies) {
                reason.append("Leave Policies");
                refCount++;
            }
            if (referencedInApplications) {
                if (refCount > 0) reason.append(", ");
                reason.append("Leave Applications");
                refCount++;
            }
            if (referencedInBalances) {
                if (refCount > 0) reason.append(", ");
                reason.append("Leave Balances");
            }

            log.info("Soft deleted leave type: {} (id: {}) - {}", existing.getCode(), id, reason);
            return new DeleteLeaveTypeResponse(
                    true,
                    "Leave type has been deactivated (soft deleted) because it is in use",
                    false,
                    reason.toString()
            );
        } else {
            // Hard delete - permanently remove
            leaveTypeRepository.delete(existing);
            log.info("Hard deleted leave type: {} (id: {})", existing.getCode(), id);
            return new DeleteLeaveTypeResponse(
                    true,
                    "Leave type has been permanently deleted",
                    true,
                    null
            );
        }
    }

    // ========== Private Validation Methods ==========

    private void validateRequiredFields(LeaveTypeInput input) {
        if (input.getCode() == null || input.getCode().isBlank()) {
            throw new IllegalArgumentException("Code is required");
        }
        if (input.getName() == null || input.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (input.getCategory() == null) {
            throw new IllegalArgumentException("Category is required - must be one of: DEFINED, EARNED, UNLIMITED, COMPENSATORY, PERMISSION");
        }
    }

    private String validateAndNormalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code cannot be blank");
        }

        String trimmedCode = code.trim();
        if (trimmedCode.length() > MAX_CODE_LENGTH) {
            throw new IllegalArgumentException("Code cannot exceed " + MAX_CODE_LENGTH + " characters");
        }

        // Auto-convert to uppercase
        return trimmedCode.toUpperCase();
    }

    private void validateNameLength(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Name cannot exceed " + MAX_NAME_LENGTH + " characters");
        }
    }

    private void validateColorCode(String colorCode) {
        if (!HEX_COLOR_PATTERN.matcher(colorCode).matches()) {
            throw new IllegalArgumentException("INVALID_COLOR - Color code must be in hex format (e.g., #3B82F6)");
        }
    }
}
