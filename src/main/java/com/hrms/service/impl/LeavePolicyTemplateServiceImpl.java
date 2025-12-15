package com.hrms.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.dto.LeaveEntitlement;
import com.hrms.dto.response.DeleteResponse;
import com.hrms.entity.Company;
import com.hrms.entity.Employee;
import com.hrms.entity.LeavePolicyTemplate;
import com.hrms.entity.LeaveType;
import com.hrms.enums.LeaveCategory;
import com.hrms.graphql.input.LeavePolicyTemplateInput;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeavePolicyTemplateRepository;
import com.hrms.repository.LeaveTypeRepository;
import com.hrms.service.LeavePolicyTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service implementation for Leave Policy Template operations.
 * Implements comprehensive validation and template matching logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeavePolicyTemplateServiceImpl implements LeavePolicyTemplateService {

    private final LeavePolicyTemplateRepository leavePolicyTemplateRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final ObjectMapper objectMapper;

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9_-]+$");
    private static final int MAX_CODE_LENGTH = 50;
    private static final int MAX_NAME_LENGTH = 200;

    @Override
    @Transactional(readOnly = true)
    public List<LeavePolicyTemplate> getLeavePolicyTemplates(String tenantId, Long companyId, Boolean isActive) {
        log.debug("Fetching leave policy templates for tenant: {}, company: {}, isActive: {}", tenantId, companyId, isActive);
        return leavePolicyTemplateRepository.findByTenantAndCompany(tenantId, companyId, isActive);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LeavePolicyTemplate> getLeavePolicyTemplate(String tenantId, Long companyId, Long id) {
        log.debug("Fetching leave policy template: {} for tenant: {}, company: {}", id, tenantId, companyId);
        return leavePolicyTemplateRepository.findByTenantAndCompanyAndId(tenantId, companyId, id);
    }

    @Override
    @Transactional(readOnly = true)
    public LeavePolicyTemplate getApplicableLeavePolicyTemplate(String tenantId, Long companyId, Long employeeId, LocalDate effectiveDate) {
        log.debug("Finding applicable template for employee: {}, date: {}", employeeId, effectiveDate);

        // Get employee data for criteria matching
        Employee employee = employeeRepository.findByTenantIdAndId(tenantId, employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        // Get all active templates within effective date range
        List<LeavePolicyTemplate> applicableTemplates = leavePolicyTemplateRepository
                .findApplicableTemplates(tenantId, companyId, effectiveDate);

        // Filter by criteria match and sort by priority
        for (LeavePolicyTemplate template : applicableTemplates) {
            if (matchesCriteria(template, employee)) {
                log.debug("Found matching template: {} for employee: {}", template.getCode(), employeeId);
                return template;
            }
        }

        // Fall back to default template
        LeavePolicyTemplate defaultTemplate = leavePolicyTemplateRepository
                .findDefaultTemplate(tenantId, companyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No applicable leave policy template found and no default template exists for company: " + companyId));

        log.debug("Using default template: {} for employee: {}", defaultTemplate.getCode(), employeeId);
        return defaultTemplate;
    }

    @Override
    public LeavePolicyTemplate createLeavePolicyTemplate(String tenantId, Long companyId, LeavePolicyTemplateInput input) {
        log.info("Creating leave policy template: {} for tenant: {}, company: {}", input.getCode(), tenantId, companyId);

        // Validate company exists
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        // Perform comprehensive validation
        validateInput(input, tenantId, companyId, null);

        // Handle default template logic
        if (Boolean.TRUE.equals(input.getIsDefault())) {
            handleDefaultTemplateCreation(tenantId, companyId, null);
        }

        // Create entity
        LeavePolicyTemplate template = new LeavePolicyTemplate();
        template.setTenantId(tenantId);
        template.setCompany(company);
        template.setCode(input.getCode());
        template.setName(input.getName());
        template.setDescription(input.getDescription());
        template.setLeaveYearStart(input.getLeaveYearStart() != null ? input.getLeaveYearStart() : 1);
        template.setCriteria(parseCriteriaJson(input.getCriteria()));
        template.setEntitlements(input.getEntitlements());
        template.setEffectiveFrom(input.getEffectiveFrom());
        template.setEffectiveTo(input.getEffectiveTo());
        template.setIsActive(input.getIsActive() != null ? input.getIsActive() : true);
        template.setIsDefault(input.getIsDefault() != null ? input.getIsDefault() : false);
        template.setPriority(input.getPriority() != null ? input.getPriority() : 0);
        template.setChangeNotes(input.getChangeNotes());
        template.setVersion(1);
        template.setCreatedBy(tenantId); // TODO: Get from security context
        template.setUpdatedBy(tenantId); // TODO: Get from security context

        LeavePolicyTemplate saved = leavePolicyTemplateRepository.save(template);
        log.info("Successfully created leave policy template: {} with ID: {}", saved.getCode(), saved.getId());
        return saved;
    }

    @Override
    public LeavePolicyTemplate updateLeavePolicyTemplate(String tenantId, Long companyId, Long id, LeavePolicyTemplateInput input) {
        log.info("Updating leave policy template: {} for tenant: {}, company: {}", id, tenantId, companyId);

        // Fetch existing template
        LeavePolicyTemplate existing = leavePolicyTemplateRepository.findByTenantAndCompanyAndId(tenantId, companyId, id)
                .orElseThrow(() -> new IllegalArgumentException("Leave policy template not found: " + id));

        // Perform comprehensive validation
        validateInput(input, tenantId, companyId, id);

        // Handle default template logic
        if (Boolean.TRUE.equals(input.getIsDefault()) && !Boolean.TRUE.equals(existing.getIsDefault())) {
            handleDefaultTemplateCreation(tenantId, companyId, id);
        }

        // Update entity
        existing.setCode(input.getCode());
        existing.setName(input.getName());
        existing.setDescription(input.getDescription());
        existing.setLeaveYearStart(input.getLeaveYearStart() != null ? input.getLeaveYearStart() : 1);
        existing.setCriteria(parseCriteriaJson(input.getCriteria()));
        existing.setEntitlements(input.getEntitlements());
        existing.setEffectiveFrom(input.getEffectiveFrom());
        existing.setEffectiveTo(input.getEffectiveTo());
        existing.setIsActive(input.getIsActive() != null ? input.getIsActive() : true);
        existing.setIsDefault(input.getIsDefault() != null ? input.getIsDefault() : false);
        existing.setPriority(input.getPriority() != null ? input.getPriority() : 0);
        existing.setChangeNotes(input.getChangeNotes());
        existing.setVersion(existing.getVersion() + 1);
        existing.setUpdatedBy(tenantId); // TODO: Get from security context

        LeavePolicyTemplate updated = leavePolicyTemplateRepository.save(existing);
        log.info("Successfully updated leave policy template: {} with ID: {}", updated.getCode(), updated.getId());
        return updated;
    }

    @Override
    public DeleteResponse deleteLeavePolicyTemplate(String tenantId, Long companyId, Long id) {
        log.info("Deleting leave policy template: {} for tenant: {}, company: {}", id, tenantId, companyId);

        // Fetch existing template
        LeavePolicyTemplate existing = leavePolicyTemplateRepository.findByTenantAndCompanyAndId(tenantId, companyId, id)
                .orElseThrow(() -> new IllegalArgumentException("Leave policy template not found: " + id));

        // Prevent deletion of default template if it's the only active one
        if (Boolean.TRUE.equals(existing.getIsDefault())) {
            long activeCount = leavePolicyTemplateRepository.findByTenantAndCompany(tenantId, companyId, true).size();
            if (activeCount == 1) {
                throw new IllegalArgumentException("Cannot delete the only active default template");
            }
        }

        // Soft delete: Mark as inactive
        existing.setIsActive(false);
        existing.setUpdatedBy(tenantId); // TODO: Get from security context
        leavePolicyTemplateRepository.save(existing);

        log.info("Successfully deleted (soft) leave policy template: {}", id);
        return new DeleteResponse(true, "Leave policy template deleted successfully");
    }

    // ===========================
    // Private Helper Methods
    // ===========================

    /**
     * Validate leave policy template input with comprehensive checks.
     */
    private void validateInput(LeavePolicyTemplateInput input, String tenantId, Long companyId, Long excludeId) {
        List<String> errors = new ArrayList<>();

        // 1. Code validation
        if (input.getCode() == null || input.getCode().isBlank()) {
            errors.add("Code is required");
        } else {
            if (input.getCode().length() > MAX_CODE_LENGTH) {
                errors.add("Code must not exceed " + MAX_CODE_LENGTH + " characters");
            }
            if (!CODE_PATTERN.matcher(input.getCode()).matches()) {
                errors.add("Code must contain only uppercase letters, numbers, hyphens, and underscores");
            }
            // Check uniqueness
            boolean codeExists = excludeId == null
                    ? leavePolicyTemplateRepository.existsByTenantAndCompanyAndCode(tenantId, companyId, input.getCode())
                    : leavePolicyTemplateRepository.existsByTenantAndCompanyAndCodeExcludingId(tenantId, companyId, input.getCode(), excludeId);
            if (codeExists) {
                errors.add("Code already exists: " + input.getCode());
            }
        }

        // 2. Name validation
        if (input.getName() == null || input.getName().isBlank()) {
            errors.add("Name is required");
        } else if (input.getName().length() > MAX_NAME_LENGTH) {
            errors.add("Name must not exceed " + MAX_NAME_LENGTH + " characters");
        }

        // 3. Leave year start validation
        if (input.getLeaveYearStart() != null && (input.getLeaveYearStart() < 1 || input.getLeaveYearStart() > 12)) {
            errors.add("Leave year start must be between 1 and 12");
        }

        // 4. Effective date validation
        if (input.getEffectiveFrom() == null) {
            errors.add("Effective from date is required");
        } else if (input.getEffectiveTo() != null && input.getEffectiveFrom().isAfter(input.getEffectiveTo())) {
            errors.add("Effective from date must be before or equal to effective to date");
        }

        // 5. Entitlements validation
        if (input.getEntitlements() == null || input.getEntitlements().isEmpty()) {
            errors.add("At least one entitlement is required");
        } else {
            validateEntitlements(input.getEntitlements(), tenantId, companyId, errors);
        }

        // 6. Default template validation
        if (Boolean.TRUE.equals(input.getIsDefault())) {
            // Default template must have empty criteria
            if (input.getCriteria() != null && !input.getCriteria().trim().isEmpty()
                && !input.getCriteria().equals("{}") && !input.getCriteria().equals("[]")) {
                errors.add("Default template must have empty criteria");
            }
        }

        // Throw exception if any errors
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join("; ", errors));
        }
    }

    /**
     * Validate entitlements array and resolve leaveTypeCode from leaveTypeId if needed.
     */
    private void validateEntitlements(List<LeaveEntitlement> entitlements, String tenantId, Long companyId, List<String> errors) {
        // Check for at least one enabled entitlement
        boolean hasEnabled = entitlements.stream().anyMatch(e -> Boolean.TRUE.equals(e.getIsEnabled()));
        if (!hasEnabled) {
            errors.add("At least one entitlement must be enabled");
        }

        // Check for duplicate leave type codes
        Set<String> leaveTypeCodes = new HashSet<>();
        for (LeaveEntitlement entitlement : entitlements) {
            LeaveType leaveType = null;

            // Validate that at least one of leaveTypeId or leaveTypeCode is provided
            if ((entitlement.getLeaveTypeCode() == null || entitlement.getLeaveTypeCode().isBlank())
                && entitlement.getLeaveTypeId() == null) {
                errors.add("Either leaveTypeCode or leaveTypeId must be provided");
                continue;
            }

            // Case 1: leaveTypeCode is provided (preferred)
            if (entitlement.getLeaveTypeCode() != null && !entitlement.getLeaveTypeCode().isBlank()) {
                Optional<LeaveType> leaveTypeOpt = leaveTypeRepository.findByTenantAndCompanyAndCode(
                    tenantId, companyId, entitlement.getLeaveTypeCode());
                if (leaveTypeOpt.isEmpty()) {
                    errors.add("Leave type with code '" + entitlement.getLeaveTypeCode() + "' not found for company " + companyId);
                    continue;
                }
                leaveType = leaveTypeOpt.get();
            }
            // Case 2: leaveTypeId is provided (backward compatibility)
            else if (entitlement.getLeaveTypeId() != null) {
                Optional<LeaveType> leaveTypeOpt = leaveTypeRepository
                    .findByTenantIdAndCompanyIdAndId(tenantId, companyId, entitlement.getLeaveTypeId());
                if (leaveTypeOpt.isEmpty()) {
                    errors.add("Leave type with ID " + entitlement.getLeaveTypeId() + " not found for company " + companyId);
                    continue;
                }
                leaveType = leaveTypeOpt.get();
                // Map ID to code for internal storage
                entitlement.setLeaveTypeCode(leaveType.getCode());
                log.debug("Mapped leaveTypeId {} to leaveTypeCode {}", entitlement.getLeaveTypeId(), leaveType.getCode());
            }

            // Check for duplicates
            if (!leaveTypeCodes.add(entitlement.getLeaveTypeCode())) {
                errors.add("Duplicate leave type code: " + entitlement.getLeaveTypeCode());
                continue;
            }

            // Validate leave type is active
            if (!Boolean.TRUE.equals(leaveType.getIsActive())) {
                errors.add("Leave type is inactive: " + entitlement.getLeaveTypeCode());
            }

            // Perform category-specific validation
            validateEntitlementByCategory(entitlement, leaveType.getCategory(), errors);
        }
    }

    /**
     * Validate entitlement based on leave category.
     */
    private void validateEntitlementByCategory(LeaveEntitlement entitlement, LeaveCategory category, List<String> errors) {
        String prefix = "Entitlement [" + entitlement.getLeaveTypeCode() + "]";

        switch (category) {
            case DEFINED:
                // Require credit frequency and corresponding credit amount
                if (entitlement.getCreditFrequency() == null) {
                    errors.add(prefix + ": Credit frequency is required for DEFINED category");
                } else {
                    switch (entitlement.getCreditFrequency()) {
                        case ANNUAL:
                            if (entitlement.getAnnualQuota() == null || entitlement.getAnnualQuota() <= 0) {
                                errors.add(prefix + ": Annual quota must be greater than 0 for ANNUAL frequency");
                            }
                            break;
                        case MONTHLY:
                            if (entitlement.getMonthlyCredit() == null || entitlement.getMonthlyCredit() <= 0) {
                                errors.add(prefix + ": Monthly credit must be greater than 0 for MONTHLY frequency");
                            }
                            break;
                        case QUARTERLY:
                            if (entitlement.getQuarterlyCredit() == null || entitlement.getQuarterlyCredit() <= 0) {
                                errors.add(prefix + ": Quarterly credit must be greater than 0 for QUARTERLY frequency");
                            }
                            break;
                        default:
                            // Other frequencies not applicable for DEFINED category
                            break;
                    }
                }
                break;

            case EARNED:
                // Require working days configuration
                if (entitlement.getWorkingDaysPerCredit() == null || entitlement.getWorkingDaysPerCredit() <= 0) {
                    errors.add(prefix + ": Working days per credit must be greater than 0 for EARNED category");
                }
                if (entitlement.getCreditPerWorkingDays() == null || entitlement.getCreditPerWorkingDays() <= 0) {
                    errors.add(prefix + ": Credit per working days must be greater than 0 for EARNED category");
                }
                break;

            case UNLIMITED:
                // No specific validation for UNLIMITED category
                break;

            case COMPENSATORY:
                // Require comp-off configuration
                if (entitlement.getCompOffExpiryDays() == null || entitlement.getCompOffExpiryDays() <= 0) {
                    errors.add(prefix + ": Comp-off expiry days must be greater than 0 for COMPENSATORY category");
                }
                if (entitlement.getCompOffMinHoursFullDay() == null || entitlement.getCompOffMinHoursFullDay() <= 0) {
                    errors.add(prefix + ": Min hours for full day comp-off must be greater than 0 for COMPENSATORY category");
                }
                if (entitlement.getCompOffMinHoursHalfDay() == null || entitlement.getCompOffMinHoursHalfDay() <= 0) {
                    errors.add(prefix + ": Min hours for half day comp-off must be greater than 0 for COMPENSATORY category");
                }
                break;

            case PERMISSION:
                // Require permission configuration
                if (entitlement.getPermissionLimitType() == null) {
                    errors.add(prefix + ": Permission limit type is required for PERMISSION category");
                }
                if (entitlement.getPermissionExcessHandling() == null) {
                    errors.add(prefix + ": Permission excess handling is required for PERMISSION category");
                }
                if (entitlement.getPermissionMaxInstancesPerMonth() == null || entitlement.getPermissionMaxInstancesPerMonth() <= 0) {
                    errors.add(prefix + ": Max instances per month must be greater than 0 for PERMISSION category");
                }
                if (entitlement.getPermissionMaxHoursPerMonth() == null || entitlement.getPermissionMaxHoursPerMonth() <= 0) {
                    errors.add(prefix + ": Max hours per month must be greater than 0 for PERMISSION category");
                }
                break;

            default:
                errors.add(prefix + ": Unknown leave category: " + category);
        }
    }

    /**
     * Handle default template creation/update logic.
     * Unset existing default template if creating a new one.
     */
    private void handleDefaultTemplateCreation(String tenantId, Long companyId, Long excludeId) {
        Optional<LeavePolicyTemplate> existingDefault = excludeId == null
                ? leavePolicyTemplateRepository.findDefaultTemplate(tenantId, companyId)
                : leavePolicyTemplateRepository.findExistingDefaultTemplateExcluding(tenantId, companyId, excludeId);

        if (existingDefault.isPresent()) {
            LeavePolicyTemplate existing = existingDefault.get();
            existing.setIsDefault(false);
            leavePolicyTemplateRepository.save(existing);
            log.debug("Unset existing default template: {}", existing.getCode());
        }
    }

    /**
     * Check if employee matches template criteria.
     * Criteria structure: {
     *   "departments": ["DEPT001"],
     *   "designations": ["DESIG001"],
     *   "grades": ["GRADE001"],
     *   "classifications": ["STAFF"],
     *   "employmentTypes": ["PERMANENT"],
     *   "locations": ["LOC001"]
     * }
     *
     * Rules:
     * - Empty criteria = matches all (default template)
     * - Multiple criteria are AND conditions
     * - Multiple values within a criterion are OR conditions
     */
    private boolean matchesCriteria(LeavePolicyTemplate template, Employee employee) {
        Map<String, Object> criteria = template.getCriteria();

        // Empty criteria = default template (matches all)
        if (criteria == null || criteria.isEmpty()) {
            return true;
        }

        // Check each criterion
        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Skip if value is null or not a list
            if (!(value instanceof List)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            List<String> allowedValues = (List<String>) value;

            // Skip if empty list
            if (allowedValues.isEmpty()) {
                continue;
            }

            // Check if employee attribute matches any of the allowed values
            boolean matches = switch (key) {
                case "departments" -> employee.getDepartment() != null &&
                        allowedValues.contains(employee.getDepartment().getCode());
                case "designations" -> employee.getDesignation() != null &&
                        allowedValues.contains(employee.getDesignation().getCode());
                case "grades" -> employee.getGrade() != null &&
                        allowedValues.contains(employee.getGrade().getCode());
                case "classifications" -> {
                    // TODO: Add classification field to Employee entity or map to JobFunction
                    // For now, skip classification matching
                    log.warn("Classification criteria not yet implemented");
                    yield true;
                }
                case "employmentTypes" -> employee.getEmploymentType() != null &&
                        allowedValues.contains(employee.getEmploymentType().getCode());
                case "locations" -> employee.getLocation() != null &&
                        allowedValues.contains(employee.getLocation().getCode());
                default -> true; // Unknown criteria, skip
            };

            // If any criterion doesn't match, template doesn't match
            if (!matches) {
                return false;
            }
        }

        // All criteria matched
        return true;
    }

    /**
     * Parse criteria JSON string to Map.
     * Returns empty map if criteria is null or empty string.
     */
    private Map<String, Object> parseCriteriaJson(String criteriaJson) {
        if (criteriaJson == null || criteriaJson.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(criteriaJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse criteria JSON: {}", criteriaJson, e);
            throw new IllegalArgumentException("Invalid criteria JSON format: " + e.getMessage());
        }
    }
}
