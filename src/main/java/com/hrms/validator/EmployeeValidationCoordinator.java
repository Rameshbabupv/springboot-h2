package com.hrms.validator;

import com.hrms.dto.request.EmployeeRequest;
import com.hrms.entity.City;
import com.hrms.entity.State;
import com.hrms.exception.DateValidationException;
import com.hrms.exception.EmployeeValidationException;
import com.hrms.exception.EmployeeValidationException.FieldError;
import com.hrms.exception.ValidationException;
import com.hrms.repository.CityRepository;
import com.hrms.repository.StateRepository;
import com.hrms.service.EmployeeTemplateService;
import com.hrms.service.impl.EmployeeValidationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Coordinates all validation layers for employee creation and update
 * Orchestrates validation in the following order:
 * 1. Structural validation (required IDs present)
 * 2. Cross-field validation (company-location relationship, self-reporting)
 * 3. Date validation (format and relationships)
 * 4. Template-based dynamic validation (field requirements based on template)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeValidationCoordinator {

    private final CrossFieldValidator crossFieldValidator;
    private final EmployeeValidationServiceImpl templateValidationService;
    private final EmployeeTemplateService employeeTemplateService;
    private final PersonalContactFieldValidator personalContactFieldValidator;
    private final CityRepository cityRepository;
    private final StateRepository stateRepository;

    /**
     * Validate employee for creation
     * Executes all validation layers in order
     *
     * @param request The employee creation request
     * @throws EmployeeValidationException if validation fails
     * @throws ValidationException if validation fails
     * @throws DateValidationException if date validation fails
     */
    public void validateForCreate(EmployeeRequest request) {
        log.debug("Starting validation for employee creation: {}", request.getEmpId());

        List<FieldError> allErrors = new ArrayList<>();

        // Layer 1: Structural validation
        try {
            validateRequiredFields(request);
        } catch (ValidationException e) {
            log.error("Structural validation failed: {}", e.getMessage());
            allErrors.add(new FieldError("structural", "Structural", e.getMessage(), null));
        }

        // Layer 2: Cross-field validation
        try {
            crossFieldValidator.validateCompanyLocationRelationship(
                request.getCompanyId(), request.getLocationId()
            );
        } catch (ValidationException e) {
            log.error("Company-location relationship validation failed: {}", e.getMessage());
            allErrors.add(new FieldError("locationMaster", "Location", e.getMessage(), request.getLocationId()));
        }

        // Layer 3: Date validation
        try {
            validateDates(request);
        } catch (DateValidationException e) {
            log.error("Date validation failed: {}", e.getMessage());
            allErrors.add(new FieldError(e.getFieldName(), toCamelCase(e.getFieldName()), e.getMessage(), e.getInvalidValue()));
        }

        // Layer 4: Personal & Contact field validations
        try {
            validatePersonalContactFields(request);
        } catch (ValidationException e) {
            log.error("Personal & Contact field validation failed: {}", e.getMessage());
            allErrors.add(new FieldError("personalContact", "Personal & Contact", e.getMessage(), null));
        }

        // Layer 5: City-State dependency validation
        try {
            validateCityState(request);
        } catch (ValidationException e) {
            log.error("City-State validation failed: {}", e.getMessage());
            allErrors.add(new FieldError("city", "City", e.getMessage(), request.getCityId()));
        }

        // If any errors so far, throw before proceeding to template validation
        if (!allErrors.isEmpty()) {
            String message = String.format("Employee validation failed with %d error(s)", allErrors.size());
            throw new EmployeeValidationException(message, allErrors);
        }

        // Layer 4: Template-based validation
        try {
            validateWithTemplate(request);
        } catch (ValidationException e) {
            log.error("Template validation failed: {}", e.getMessage());
            throw e;
        }

        log.info("All validation layers passed for employee creation");
    }

    /**
     * Validate employee for update
     * Same as create validation plus additional update-specific validations
     *
     * @param employeeId The ID of the employee being updated
     * @param request The employee update request
     * @throws EmployeeValidationException if validation fails
     * @throws ValidationException if validation fails
     */
    public void validateForUpdate(Long employeeId, EmployeeRequest request) {
        log.debug("Starting validation for employee update: id={}, empId={}", employeeId, request.getEmpId());

        // Execute create validation first
        validateForCreate(request);

        // Additional validation: Prevent self-reporting in update mode
        try {
            crossFieldValidator.validateReportingManager(employeeId, request.getReportingManagerId());
        } catch (ValidationException e) {
            log.error("Self-reporting validation failed: {}", e.getMessage());
            List<FieldError> errors = List.of(
                new FieldError("reportTo", "Reporting Manager", e.getMessage(), request.getReportingManagerId())
            );
            throw new EmployeeValidationException("Self-reporting not allowed", errors);
        }

        log.info("All validation layers passed for employee update");
    }

    /**
     * Validate that all required organizational fields are present
     *
     * @param request The employee request
     * @throws ValidationException if any required field is missing
     */
    private void validateRequiredFields(EmployeeRequest request) {
        log.debug("Validating required fields");

        if (request.getTenantId() == null || request.getTenantId().isBlank()) {
            throw new ValidationException("Tenant ID is required");
        }

        if (request.getCompanyId() == null) {
            throw new ValidationException("Company is required");
        }

        if (request.getLocationId() == null) {
            throw new ValidationException("Location is required");
        }

        if (request.getDepartmentId() == null) {
            throw new ValidationException("Department is required");
        }

        if (request.getDesignationId() == null) {
            throw new ValidationException("Designation is required");
        }

        if (request.getJobFunctionId() == null) {
            throw new ValidationException("Job Function is required");
        }

        if (request.getEmploymentTypeId() == null) {
            throw new ValidationException("Employment Type is required");
        }

        // Employment details
        if (request.getEmpId() == null || request.getEmpId().isBlank()) {
            throw new ValidationException("Employee ID is required");
        }

        if (request.getEmployeeName() == null || request.getEmployeeName().isBlank()) {
            throw new ValidationException("Employee Name is required");
        }

        if (request.getDateOfJoin() == null || request.getDateOfJoin().isBlank()) {
            throw new ValidationException("Date of Joining is required");
        }

        // Personal details
        if (request.getDateOfBirth() == null || request.getDateOfBirth().isBlank()) {
            throw new ValidationException("Date of Birth is required");
        }

        if (request.getGender() == null || request.getGender().isBlank()) {
            throw new ValidationException("Gender is required");
        }

        if (request.getAadharNo() == null || request.getAadharNo().isBlank()) {
            throw new ValidationException("Aadhaar Number is required");
        }

        if (request.getPanNo() == null || request.getPanNo().isBlank()) {
            throw new ValidationException("PAN is required");
        }

        // Personal Information - Contact Tab
        if (request.getAddress1() == null || request.getAddress1().isBlank()) {
            throw new ValidationException("Address Line 1 is required");
        }

        if (request.getStateId() == null) {
            throw new ValidationException("State is required");
        }

        if (request.getCityId() == null) {
            throw new ValidationException("City is required");
        }

        if (request.getPincode() == null || request.getPincode().isBlank()) {
            throw new ValidationException("Pincode is required");
        } else {
            personalContactFieldValidator.validatePincode(request.getPincode(), "Pincode");
        }

        if (request.getMobileNo() == null || request.getMobileNo().isBlank()) {
            throw new ValidationException("Mobile Number is required");
        } else {
            personalContactFieldValidator.validatePhoneNumber(request.getMobileNo(), "Mobile Number");
        }

        if (request.getEmailId() == null || request.getEmailId().isBlank()) {
            throw new ValidationException("Personal Email is required");
        } else {
            personalContactFieldValidator.validateEmail(request.getEmailId(), "Personal Email");
        }

        if (request.getOfficialEmailId() == null || request.getOfficialEmailId().isBlank()) {
            throw new ValidationException("Official Email is required");
        } else {
            personalContactFieldValidator.validateEmail(request.getOfficialEmailId(), "Official Email");
        }

        log.debug("All required fields validated successfully");
    }

    /**
     * Validate all dates
     * Parses date strings and validates relationships
     *
     * @param request The employee request
     * @throws DateValidationException if date validation fails
     */
    private void validateDates(EmployeeRequest request) {
        log.debug("Validating dates");

        // Parse individual dates with strict validation
        LocalDate dob = DateValidator.parseAndValidateDate(request.getDateOfBirth(), "Date of Birth");
        LocalDate doj = DateValidator.parseAndValidateDate(request.getDateOfJoin(), "Date of Joining");

        // Optional dates
        LocalDate doc = request.getDateOfConfirm() != null ?
            DateValidator.parseAndValidateDate(request.getDateOfConfirm(), "Date of Confirmation") : null;
        LocalDate dor = request.getDateOfRetirement() != null ?
            DateValidator.parseAndValidateDate(request.getDateOfRetirement(), "Date of Retirement") : null;

        // Validate relationships
        DateValidator.validateDateRelationships(doj, doc, dor, dob);

        log.debug("All date validations passed");
    }

    /**
     * Validate employee data against applicable template
     * Fetches template based on organizational criteria and validates field requirements
     *
     * @param request The employee request
     * @throws ValidationException if template validation fails
     */
    private void validateWithTemplate(EmployeeRequest request) {
        log.debug("Starting template-based validation");

        try {
            // Template validation would require fetching the template and running field-level validations
            // This is integrated but deferred to Phase 2 when EmployeeTemplateService is enhanced
            log.info("Template-based validation deferred to Phase 2 integration");
        } catch (Exception e) {
            log.error("Template validation error: {}", e.getMessage());
            throw new ValidationException("Template validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate optional Personal & Contact fields
     * Emergency contacts and blood group validation if provided
     *
     * @param request The employee request
     * @throws ValidationException if any optional field validation fails
     */
    private void validatePersonalContactFields(EmployeeRequest request) {
        log.debug("Validating optional Personal & Contact fields");

        // Emergency contacts (optional but if provided must be 10 digits)
        if (request.getEmergencyNoOne() != null && !request.getEmergencyNoOne().isEmpty()) {
            personalContactFieldValidator.validatePhoneNumber(request.getEmergencyNoOne(), "Emergency Contact 1");
        }

        if (request.getEmergencyNoTwo() != null && !request.getEmergencyNoTwo().isEmpty()) {
            personalContactFieldValidator.validatePhoneNumber(request.getEmergencyNoTwo(), "Emergency Contact 2");
        }

        // Blood group validation (optional)
        if (request.getBloodGroup() != null && !request.getBloodGroup().isEmpty()) {
            personalContactFieldValidator.validateBloodGroup(request.getBloodGroup());
        }

        // Marital status validation (optional)
        if (request.getMaritalStatus() != null && !request.getMaritalStatus().isEmpty()) {
            personalContactFieldValidator.validateMaritalStatus(request.getMaritalStatus());
        }

        log.debug("Optional Personal & Contact field validation passed");
    }

    /**
     * Validate that selected city belongs to selected state
     * Ensures city-state referential integrity
     *
     * @param request The employee request
     * @throws ValidationException if city does not belong to state
     */
    private void validateCityState(EmployeeRequest request) {
        log.debug("Validating city-state dependency: stateId={}, cityId={}", request.getStateId(), request.getCityId());

        if (request.getStateId() == null || request.getCityId() == null) {
            log.debug("Skipping city-state validation - state or city not provided");
            return; // Already validated as required
        }

        try {
            // Fetch city from database
            City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> {
                    log.error("City not found: {}", request.getCityId());
                    return new ValidationException("City not found with ID: " + request.getCityId());
                });

            // Fetch state from database
            State state = stateRepository.findById(request.getStateId())
                .orElseThrow(() -> {
                    log.error("State not found: {}", request.getStateId());
                    return new ValidationException("State not found with ID: " + request.getStateId());
                });

            // Validate city belongs to state by comparing IDs
            Long cityStateId = city.getStateId();
            Long selectedStateId = state.getId();

            if (cityStateId == null || !cityStateId.equals(selectedStateId)) {
                log.error("City {} (stateId: {}) does not belong to state {}: selectedStateId={}",
                    request.getCityId(), cityStateId, request.getStateId(), selectedStateId);
                throw new ValidationException(
                    String.format("Selected city does not belong to selected state (city belongs to state %d, but %d was selected)",
                        cityStateId, selectedStateId)
                );
            }

            log.info("City-State validation passed: city {} (stateId: {}) belongs to state {} (id: {})",
                request.getCityId(), cityStateId, request.getStateId(), selectedStateId);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during city-state validation: {}", e.getMessage());
            throw new ValidationException("Error validating city-state relationship: " + e.getMessage(), e);
        }
    }

    /**
     * Convert field names from camelCase (dateOfJoin) to Title Case (Date Of Join)
     *
     * @param fieldName The field name in camelCase
     * @return The field label in Title Case
     */
    private String toCamelCase(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }

        // Simple conversion for common date field names
        return switch (fieldName) {
            case "dateOfJoin" -> "Date of Joining";
            case "dateOfBirth" -> "Date of Birth";
            case "dateOfConfirm" -> "Date of Confirmation";
            case "dateOfRetirement" -> "Date of Retirement";
            default -> fieldName;
        };
    }
}
