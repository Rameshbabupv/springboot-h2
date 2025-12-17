package com.hrms.validator;

import com.hrms.entity.CompanyLocation;
import com.hrms.exception.ValidationException;
import com.hrms.repository.CompanyLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator for cross-field relationships and constraints
 * Ensures data consistency across multiple fields
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrossFieldValidator {

    private final CompanyLocationRepository companyLocationRepository;

    /**
     * Validate that employee does not report to themselves (self-reporting prevention)
     * Only validates in update mode (when employeeId is provided)
     *
     * @param employeeId The current employee's ID (null in create mode, set in update mode)
     * @param reportingManagerId The ID of the reporting manager
     * @throws ValidationException if employee reports to themselves
     */
    public void validateReportingManager(Long employeeId, Long reportingManagerId) {
        // Skip validation in create mode (employeeId is null)
        if (employeeId == null) {
            log.debug("Skipping self-reporting validation in create mode");
            return;
        }

        // Skip if no manager is assigned
        if (reportingManagerId == null) {
            log.debug("No reporting manager assigned, skipping validation");
            return;
        }

        // Check if employee reports to themselves
        if (employeeId.equals(reportingManagerId)) {
            log.error("Self-reporting attempted: employee {} reporting to manager {}", employeeId, reportingManagerId);
            throw new ValidationException(
                "An employee cannot report to themselves"
            );
        }

        log.debug("Reporting manager validation passed for employee {}", employeeId);
    }

    /**
     * Validate that the selected location belongs to the selected company
     * Ensures company-location relationship consistency
     *
     * @param companyId The selected company ID
     * @param locationId The selected location ID
     * @throws ValidationException if location does not belong to the company
     */
    public void validateCompanyLocationRelationship(Long companyId, Long locationId) {
        log.debug("Validating company-location relationship: company={}, location={}", companyId, locationId);

        // Validate companyId is not null
        if (companyId == null) {
            throw new ValidationException("Company ID is required");
        }

        // Validate locationId is not null
        if (locationId == null) {
            throw new ValidationException("Location ID is required");
        }

        // Fetch the location
        CompanyLocation location = companyLocationRepository.findById(locationId)
            .orElseThrow(() -> {
                log.error("Location not found: {}", locationId);
                return new ValidationException("Location not found with ID: " + locationId);
            });

        // Validate location belongs to selected company
        if (!location.getCompany().getId().equals(companyId)) {
            log.error("Location {} does not belong to company {}: belongs to company {}",
                locationId, companyId, location.getCompany().getId());
            throw new ValidationException(
                String.format("Location '%s' (ID: %d) does not belong to the selected company",
                    location.getName(), locationId)
            );
        }

        log.info("Company-location relationship validation passed");
    }
}
