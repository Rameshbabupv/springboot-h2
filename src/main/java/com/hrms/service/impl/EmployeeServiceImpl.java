package com.hrms.service.impl;

import com.hrms.dto.request.EmployeeFilterCriteria;
import com.hrms.dto.request.EmployeeRequest;
import com.hrms.dto.response.EmployeeResponse;
import com.hrms.dto.response.ManagerOptionResponse;
import com.hrms.entity.*;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.exception.ValidationException;
import com.hrms.mapper.EmployeeMapper;
import com.hrms.repository.*;
import com.hrms.validator.EmployeeValidationCoordinator;
import com.hrms.service.EmployeeService;
import com.hrms.service.OrganizationalScopeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Employee Service Implementation with MapStruct
 * Clean and maintainable with automatic DTO mapping
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final EntityManager entityManager;
    private final EmployeeValidationCoordinator validationCoordinator;
    private final OrganizationalScopeService organizationalScopeService;

    // Master repositories for required relationships
    private final CompanyRepository companyRepository;
    private final CompanyLocationRepository companyLocationRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final JobFunctionRepository jobFunctionRepository;
    private final EmploymentTypeRepository employmentTypeRepository;

    // Optional relationship repositories
    private final DivisionRepository divisionRepository;
    private final SectionRepository sectionRepository;
    private final GradeRepository gradeRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    // =====================================================
    // CREATE EMPLOYEE
    // =====================================================

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.debug("Creating employee: {} for tenant: {}", request.getEmpId(), request.getTenantId());

        // Auto-generate employee ID if not provided
        if (request.getEmpId() == null || request.getEmpId().isEmpty()) {
            String generatedId = generateEmployeeId(request.getTenantId(), request.getCompanyId());
            request.setEmpId(generatedId);
            log.info("Auto-generated employee ID: {}", generatedId);
        }

        // Execute multi-layer validation (structural, cross-field, date, template-based)
        validationCoordinator.validateForCreate(request);

        // Check for duplicate empId
        if (employeeRepository.findByTenantIdAndEmpId(request.getTenantId(), request.getEmpId()).isPresent()) {
            throw new ValidationException("Employee ID '" + request.getEmpId() + "' already exists for this tenant");
        }

        // Log values BEFORE mapping
        log.debug("BEFORE MAPPING - Request sourceOfHire: {}, noticePeriod: {}",
                  request.getSourceOfHire(), request.getNoticePeriod());

        // Map DTO to entity (MapStruct handles all 78 fields automatically!)
        Employee employee = employeeMapper.toEntity(request);

        // Log values AFTER mapping
        log.debug("AFTER MAPPING - Entity sourceOfHire: {}, noticePeriod: {}",
                  employee.getSourceOfHire(), employee.getNoticePeriod());

        // Set required relationships manually (foreign keys)
        setRequiredRelationships(employee, request);

        // Set optional relationships
        setOptionalRelationships(employee, request);

        // Log Personal & Contact Information
        log.debug("CREATE EMPLOYEE - Personal Info: employeeName={}, dateOfBirth={}, gender={}, fatherName={}, bloodGroup={}, maritalStatus={}",
                  employee.getEmployeeName(), employee.getDateOfBirth(), employee.getGender(),
                  employee.getFatherName(), employee.getBloodGroup(), employee.getMaritalStatus());
        log.debug("CREATE EMPLOYEE - Contact Info: address1={}, pincode={}, mobileNo={}, emailId={}, officialEmailId={}",
                  employee.getAddress1(), employee.getPincode(), employee.getMobileNo(),
                  employee.getEmailId(), employee.getOfficialEmailId());
        log.debug("CREATE EMPLOYEE - Emergency Contacts: emergencyNoOne={}, emergencyNoTwo={}",
                  employee.getEmergencyNoOne(), employee.getEmergencyNoTwo());

        // Calculate age from dateOfBirth (also calculated by trigger, but service layer as safety net)
        calculateAge(employee);

        // Save employee
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created successfully with ID: {} for tenant: {}", savedEmployee.getId(), savedEmployee.getTenantId());

        return employeeMapper.toResponse(savedEmployee);
    }

    // =====================================================
    // UPDATE EMPLOYEE
    // =====================================================

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(String tenantId, Long id, EmployeeRequest request) {
        log.debug("Updating employee ID: {} for tenant: {}", id, tenantId);

        // Fetch existing employee (tenant-aware)
        Employee existingEmployee = employeeRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        // Execute multi-layer validation including self-reporting check (update-specific)
        validationCoordinator.validateForUpdate(id, request);

        // Check for duplicate empId (if changed)
        if (!existingEmployee.getEmpId().equals(request.getEmpId())) {
            if (employeeRepository.findByTenantIdAndEmpId(request.getTenantId(), request.getEmpId()).isPresent()) {
                throw new ValidationException("Employee ID '" + request.getEmpId() + "' already exists");
            }
        }

        // Log values BEFORE mapping
        log.debug("BEFORE UPDATE MAPPING - Request sourceOfHire: {}, noticePeriod: {}",
                  request.getSourceOfHire(), request.getNoticePeriod());

        // Update entity from request (MapStruct handles all fields!)
        employeeMapper.updateEntityFromRequest(request, existingEmployee);

        // Log values AFTER mapping
        log.debug("AFTER UPDATE MAPPING - Entity sourceOfHire: {}, noticePeriod: {}",
                  existingEmployee.getSourceOfHire(), existingEmployee.getNoticePeriod());

        // Update relationships
        setRequiredRelationships(existingEmployee, request);
        setOptionalRelationships(existingEmployee, request);

        // Log Personal & Contact Information being updated
        log.debug("UPDATE EMPLOYEE - Personal Info: employeeName={}, dateOfBirth={}, gender={}, bloodGroup={}, maritalStatus={}",
                  existingEmployee.getEmployeeName(), existingEmployee.getDateOfBirth(), existingEmployee.getGender(),
                  existingEmployee.getBloodGroup(), existingEmployee.getMaritalStatus());
        log.debug("UPDATE EMPLOYEE - Contact Info: mobileNo={}, emailId={}, pincode={}, stateId={}, cityId={}",
                  existingEmployee.getMobileNo(), existingEmployee.getEmailId(), existingEmployee.getPincode(),
                  existingEmployee.getState() != null ? existingEmployee.getState().getId() : null,
                  existingEmployee.getCity() != null ? existingEmployee.getCity().getId() : null);

        // Recalculate age if dateOfBirth was updated
        calculateAge(existingEmployee);

        // Save updated employee
        Employee savedEmployee = employeeRepository.save(existingEmployee);
        log.info("Employee updated successfully with ID: {}", savedEmployee.getId());

        return employeeMapper.toResponse(savedEmployee);
    }

    // =====================================================
    // READ OPERATIONS
    // =====================================================

    @Override
    public EmployeeResponse getEmployeeById(String tenantId, Long id) {
        log.debug("Fetching employee ID: {} for tenant: {}", id, tenantId);
        Employee employee = employeeRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return employeeMapper.toResponse(employee);
    }

    @Override
    public EmployeeResponse getEmployeeByEmpId(String tenantId, String empId) {
        log.debug("Fetching employee by empId: {} for tenant: {}", empId, tenantId);
        Employee employee = employeeRepository.findByTenantIdAndEmpId(tenantId, empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "empId", empId));
        return employeeMapper.toResponse(employee);
    }

    @Override
    public List<EmployeeResponse> getAllEmployeesByTenant(String tenantId) {
        log.debug("Fetching all employees for tenant: {}", tenantId);
        return employeeRepository.findByTenantId(tenantId).stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponse> getEmployeesByCompany(String tenantId, Long companyId) {
        log.debug("Fetching employees for company: {} and tenant: {}", companyId, tenantId);
        return employeeRepository.findByTenantIdAndCompany_Id(tenantId, companyId).stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponse> getEmployeesByDepartment(String tenantId, Long departmentId) {
        log.debug("Fetching employees for department: {} and tenant: {}", departmentId, tenantId);
        return employeeRepository.findByTenantIdAndDepartment_Id(tenantId, departmentId).stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponse> getEmployeesByDesignation(String tenantId, Long designationId) {
        log.debug("Fetching employees for designation: {} and tenant: {}", designationId, tenantId);
        return employeeRepository.findByTenantIdAndDesignation_Id(tenantId, designationId).stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponse> getEmployeesByStatus(String tenantId, String status) {
        log.debug("Fetching employees with status: {} for tenant: {}", status, tenantId);
        return employeeRepository.findByTenantIdAndEmployeeStatus(tenantId, status).stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponse> getEmployeesByReportingManager(String tenantId, Long managerId) {
        log.debug("Fetching employees reporting to manager: {} for tenant: {}", managerId, tenantId);
        return employeeRepository.findByTenantIdAndReportingManager_Id(tenantId, managerId).stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponse> searchEmployees(String tenantId, String searchTerm) {
        log.debug("Searching employees with term: {} for tenant: {}", searchTerm, tenantId);
        return employeeRepository.searchEmployees(tenantId, searchTerm).stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // DELETE OPERATION
    // =====================================================

    @Override
    @Transactional
    public void deleteEmployee(String tenantId, Long id) {
        log.debug("Deleting employee ID: {} for tenant: {}", id, tenantId);
        Employee employee = employeeRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        employeeRepository.delete(employee);
        log.info("Employee deleted successfully with ID: {}", id);
    }

    // =====================================================
    // UTILITY METHODS
    // =====================================================

    @Override
    public long getEmployeeCountByTenant(String tenantId) {
        return employeeRepository.countByTenantId(tenantId);
    }

    @Override
    public long getEmployeeCountByStatus(String tenantId, String status) {
        return employeeRepository.countByTenantIdAndStatus(tenantId, status);
    }

    @Override
    public boolean existsByEmpId(String tenantId, String empId) {
        return employeeRepository.findByTenantIdAndEmpId(tenantId, empId).isPresent();
    }

    // =====================================================
    // PRIVATE HELPER METHODS
    // =====================================================

    /**
     * Validates the 13 required fields for employee creation
     * @deprecated Use EmployeeValidationCoordinator instead
     */
    @Deprecated
    private void validateRequiredFields(EmployeeRequest request) {
        StringBuilder errors = new StringBuilder();

        // Tenant (1 field)
        if (request.getTenantId() == null || request.getTenantId().isBlank()) {
            errors.append("Tenant ID is required. ");
        }

        // Organizational (6 fields)
        if (request.getCompanyId() == null) errors.append("Company is required. ");
        if (request.getLocationId() == null) errors.append("Location is required. ");
        if (request.getDepartmentId() == null) errors.append("Department is required. ");
        if (request.getDesignationId() == null) errors.append("Designation is required. ");
        if (request.getJobFunctionId() == null) errors.append("Job Function is required. ");
        if (request.getEmploymentTypeId() == null) errors.append("Employment Type is required. ");

        // Identity (3 fields)
        if (request.getEmpId() == null || request.getEmpId().isBlank()) {
            errors.append("Employee ID is required. ");
        }
        if (request.getEmployeeName() == null || request.getEmployeeName().isBlank()) {
            errors.append("Employee Name is required. ");
        }
        if (request.getDateOfJoin() == null || request.getDateOfJoin().isBlank()) {
            errors.append("Date of Joining is required. ");
        }

        // Statutory (2 fields)
        if (request.getDateOfBirth() == null || request.getDateOfBirth().isBlank()) {
            errors.append("Date of Birth is required. ");
        }
        if (request.getGender() == null || request.getGender().isBlank()) {
            errors.append("Gender is required. ");
        }

        // Note: Aadhaar and PAN validation is done by @NotBlank in DTO

        if (errors.length() > 0) {
            throw new ValidationException("Validation failed: " + errors.toString());
        }
    }

    /**
     * Auto-generate employee ID based on company prefix and sequence
     * Format: {COMPANY_PREFIX}-{5-digit-sequence}
     * Example: ACME-00001, TECH-00002
     *
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @return Generated employee ID
     * @throws ResourceNotFoundException if company not found
     */
    @Transactional
    public String generateEmployeeId(String tenantId, Long companyId) {
        log.debug("Generating employee ID for company: {} in tenant: {}", companyId, tenantId);

        // Fetch company to get prefix
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));

        // Get prefix from company shortName or code
        String prefix = company.getShortName() != null && !company.getShortName().isEmpty()
                ? company.getShortName().toUpperCase()
                : company.getCode() != null && !company.getCode().isEmpty()
                ? company.getCode().substring(0, Math.min(4, company.getCode().length())).toUpperCase()
                : "GEN"; // Default fallback

        // Get next sequence (thread-safe database operation)
        long nextSequence = employeeRepository.getNextEmployeeSequence(tenantId, companyId);

        // Format: PREFIX-00001
        String generatedId = String.format("%s-%05d", prefix, nextSequence);
        log.info("Generated employee ID: {} for company: {}", generatedId, companyId);

        return generatedId;
    }

    /**
     * Sets required foreign key relationships (6 required masters)
     */
    private void setRequiredRelationships(Employee employee, EmployeeRequest request) {
        // Company (required)
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));
        employee.setCompany(company);

        // Location (required)
        CompanyLocation location = companyLocationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", request.getLocationId()));
        employee.setLocation(location);

        // Department (required)
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
        employee.setDepartment(department);

        // Designation (required)
        Designation designation = designationRepository.findById(request.getDesignationId())
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", request.getDesignationId()));
        employee.setDesignation(designation);

        // Job Function (required)
        JobFunction jobFunction = jobFunctionRepository.findById(request.getJobFunctionId())
                .orElseThrow(() -> new ResourceNotFoundException("JobFunction", "id", request.getJobFunctionId()));
        employee.setJobFunction(jobFunction);

        // Employment Type (required)
        EmploymentType employmentType = employmentTypeRepository.findById(request.getEmploymentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("EmploymentType", "id", request.getEmploymentTypeId()));
        employee.setEmploymentType(employmentType);
    }

    /**
     * Sets optional foreign key relationships
     */
    private void setOptionalRelationships(Employee employee, EmployeeRequest request) {
        // Division (optional)
        if (request.getDivisionId() != null) {
            Division division = divisionRepository.findById(request.getDivisionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Division", "id", request.getDivisionId()));
            employee.setDivision(division);
        }

        // Section (optional)
        if (request.getSectionId() != null) {
            Section section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.getSectionId()));
            employee.setSection(section);
        }

        // Grade (optional)
        if (request.getGradeId() != null) {
            Grade grade = gradeRepository.findById(request.getGradeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", request.getGradeId()));
            employee.setGrade(grade);
        }

        // Reporting Manager (optional, self-reference)
        if (request.getReportingManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getReportingManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reporting Manager", "id", request.getReportingManagerId()));
            employee.setReportingManager(manager);
        }

        // State (optional)
        if (request.getStateId() != null) {
            State state = stateRepository.findById(request.getStateId())
                    .orElseThrow(() -> new ResourceNotFoundException("State", "id", request.getStateId()));
            employee.setState(state);
        }

        // City (optional)
        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.getCityId()));
            employee.setCity(city);
        }
    }

    /**
     * Calculate and set age from date of birth
     * Ensures age is always derived from the actual date of birth
     * Used in conjunction with database trigger for consistency
     *
     * @param employee The employee entity with dateOfBirth populated
     */
    private void calculateAge(Employee employee) {
        if (employee.getDateOfBirth() != null) {
            try {
                // Note: Database trigger also calculates age on INSERT/UPDATE
                // This method provides an additional safety check
                int age = Year.now().getValue() - Year.from(employee.getDateOfBirth()).getValue();
                employee.setAge(age);
                log.debug("Age calculated from dateOfBirth: {} → age: {}", employee.getDateOfBirth(), age);
            } catch (Exception e) {
                log.error("Error calculating age from dateOfBirth: {}", e.getMessage());
                // Don't throw exception - age calculation is not critical
            }
        }
    }

    // =====================================================
    // NEW: ADVANCED FILTERING WITH ORGANIZATIONAL SCOPE
    // =====================================================

    @Override
    public Page<EmployeeResponse> getFilteredEmployees(EmployeeFilterCriteria criteria) {
        log.debug("Fetching filtered employees for tenant: {}", criteria.getTenantId());

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> query = cb.createQuery(Employee.class);
        Root<Employee> root = query.from(Employee.class);

        // Build WHERE clause with all filters
        List<Predicate> predicates = buildPredicates(cb, root, criteria);
        query.where(predicates.toArray(new Predicate[0]));

        // Apply sorting
        if (criteria.getSortBy() != null && !criteria.getSortBy().isEmpty()) {
            if ("DESC".equalsIgnoreCase(criteria.getSortDirection())) {
                query.orderBy(cb.desc(root.get(criteria.getSortBy())));
            } else {
                query.orderBy(cb.asc(root.get(criteria.getSortBy())));
            }
        } else {
            query.orderBy(cb.desc(root.get("id"))); // Default sort by ID desc
        }

        // Execute query with pagination
        TypedQuery<Employee> typedQuery = entityManager.createQuery(query);

        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int size = criteria.getSize() != null ? criteria.getSize() : 20;

        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);

        List<Employee> employees = typedQuery.getResultList();
        long total = countFilteredEmployees(criteria);

        // Convert to responses
        List<EmployeeResponse> responses = employees.stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());

        PageRequest pageRequest = PageRequest.of(page, size);
        return new PageImpl<>(responses, pageRequest, total);
    }

    @Override
    public long countFilteredEmployees(EmployeeFilterCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Employee> root = query.from(Employee.class);

        // Build same WHERE clause
        List<Predicate> predicates = buildPredicates(cb, root, criteria);
        query.select(cb.count(root));
        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public List<EmployeeResponse> getFilteredEmployeesList(EmployeeFilterCriteria criteria) {
        log.debug("Fetching filtered employees list for tenant: {}", criteria.getTenantId());

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> query = cb.createQuery(Employee.class);
        Root<Employee> root = query.from(Employee.class);

        // Build WHERE clause
        List<Predicate> predicates = buildPredicates(cb, root, criteria);
        query.where(predicates.toArray(new Predicate[0]));

        // Apply sorting
        if (criteria.getSortBy() != null && !criteria.getSortBy().isEmpty()) {
            if ("DESC".equalsIgnoreCase(criteria.getSortDirection())) {
                query.orderBy(cb.desc(root.get(criteria.getSortBy())));
            } else {
                query.orderBy(cb.asc(root.get(criteria.getSortBy())));
            }
        }

        List<Employee> employees = entityManager.createQuery(query).getResultList();

        return employees.stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Build predicates for filtering
     * Supports all 9 organizational parameters + search + status
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Employee> root, EmployeeFilterCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        // REQUIRED: Tenant filter
        if (criteria.getTenantId() != null) {
            predicates.add(cb.equal(root.get("tenantId"), criteria.getTenantId()));
        }

        // 1. Company filter
        if (criteria.hasCompanyFilter()) {
            predicates.add(root.get("company").get("id").in(criteria.getCompanyIds()));
        }

        // 2. Location filter with company-location relationship validation
        if (criteria.hasLocationFilter()) {
            // Employee's location must be in the allowed location IDs
            Predicate locationInScope = root.get("location").get("id").in(criteria.getLocationIds());

            // AND the employee's location must belong to the employee's company
            // This prevents invalid combinations like:
            // - Employee with Company 2 + Location 2 (when Location 2 belongs to Company 1)
            Predicate locationBelongsToCompany = cb.equal(
                root.get("location").get("company").get("id"),
                root.get("company").get("id")
            );

            predicates.add(cb.and(locationInScope, locationBelongsToCompany));
        }

        // 3. Division filter
        // Include employees with NULL division (not assigned) - they match any filter
        if (criteria.hasDivisionFilter()) {
            Predicate divisionIn = root.get("division").get("id").in(criteria.getDivisionIds());
            Predicate divisionIsNull = cb.isNull(root.get("division"));
            predicates.add(cb.or(divisionIn, divisionIsNull));
        }

        // 4. Department filter
        // Include employees with NULL department (not assigned) - they match any filter
        if (criteria.hasDepartmentFilter()) {
            Predicate departmentIn = root.get("department").get("id").in(criteria.getDepartmentIds());
            Predicate departmentIsNull = cb.isNull(root.get("department"));
            predicates.add(cb.or(departmentIn, departmentIsNull));
        }

        // 5. Section filter
        // Include employees with NULL section (not assigned) - they match any filter
        if (criteria.hasSectionFilter()) {
            Predicate sectionIn = root.get("section").get("id").in(criteria.getSectionIds());
            Predicate sectionIsNull = cb.isNull(root.get("section"));
            predicates.add(cb.or(sectionIn, sectionIsNull));
        }

        // 6. Designation filter
        // Include employees with NULL designation (not assigned) - they match any filter
        if (criteria.hasDesignationFilter()) {
            Predicate designationIn = root.get("designation").get("id").in(criteria.getDesignationIds());
            Predicate designationIsNull = cb.isNull(root.get("designation"));
            predicates.add(cb.or(designationIn, designationIsNull));
        }

        // 7. Grade filter
        // Include employees with NULL grade (not assigned) - they match any filter
        if (criteria.hasGradeFilter()) {
            Predicate gradeIn = root.get("grade").get("id").in(criteria.getGradeIds());
            Predicate gradeIsNull = cb.isNull(root.get("grade"));
            predicates.add(cb.or(gradeIn, gradeIsNull));
        }

        // 8. Job Function filter
        // Include employees with NULL job function (not assigned) - they match any filter
        if (criteria.hasJobFunctionFilter()) {
            Predicate jobFunctionIn = root.get("jobFunction").get("id").in(criteria.getJobFunctionIds());
            Predicate jobFunctionIsNull = cb.isNull(root.get("jobFunction"));
            predicates.add(cb.or(jobFunctionIn, jobFunctionIsNull));
        }

        // 9. Employment Type filter
        // Include employees with NULL employment type (not assigned) - they match any filter
        if (criteria.hasEmploymentTypeFilter()) {
            Predicate employmentTypeIn = root.get("employmentType").get("id").in(criteria.getEmploymentTypeIds());
            Predicate employmentTypeIsNull = cb.isNull(root.get("employmentType"));
            predicates.add(cb.or(employmentTypeIn, employmentTypeIsNull));
        }

        // Additional filters

        // Search query (employee name, emp_id, email)
        if (criteria.hasSearchQuery()) {
            String searchPattern = "%" + criteria.getSearchQuery().toLowerCase() + "%";
            Predicate namePredicate = cb.like(cb.lower(root.get("employeeName")), searchPattern);
            Predicate empIdPredicate = cb.like(cb.lower(root.get("empId")), searchPattern);
            Predicate emailPredicate = cb.like(cb.lower(root.get("emailId")), searchPattern);
            predicates.add(cb.or(namePredicate, empIdPredicate, emailPredicate));
        }

        // Employee status filter
        if (criteria.hasEmployeeStatusFilter()) {
            predicates.add(cb.equal(root.get("employeeStatus"), criteria.getEmployeeStatus()));
        }

        // Reporting manager filter
        if (criteria.hasReportingManagerFilter()) {
            predicates.add(cb.equal(root.get("reportingManager").get("id"), criteria.getReportingManagerId()));
        }

        return predicates;
    }

    // =====================================================
    // EMPLOYEE CREATION HELPER METHODS
    // =====================================================

    @Override
    public List<ManagerOptionResponse> getEligibleManagers(
            String tenantId, Long userId, Long companyId,
            Long currentEmployeeId, String searchTerm) {

        log.debug("Fetching eligible managers for user: {} in tenant: {}", userId, tenantId);

        // Get user's organizational scope for filtering
        var userScope = organizationalScopeService.getUserScope(tenantId, userId);

        // Build list of allowed company IDs
        java.util.List<Long> allowedCompanyIds = null;
        if (companyId != null) {
            // If specific company is requested, verify it's in user's scope
            allowedCompanyIds = java.util.List.of(companyId);
        } else if (userScope != null && userScope.getCompanyIds() != null && !userScope.getCompanyIds().isEmpty()) {
            // Use user's scope companies
            allowedCompanyIds = userScope.getCompanyIds();
        }

        // Build list of allowed location IDs
        java.util.List<Long> allowedLocationIds = null;
        if (userScope != null && userScope.getLocationIds() != null && !userScope.getLocationIds().isEmpty()) {
            allowedLocationIds = userScope.getLocationIds();
        }

        // Build list of allowed department IDs
        java.util.List<Long> allowedDepartmentIds = null;
        if (userScope != null && userScope.getDepartmentIds() != null && !userScope.getDepartmentIds().isEmpty()) {
            allowedDepartmentIds = userScope.getDepartmentIds();
        }

        // Fetch eligible managers from repository
        java.util.List<Employee> managers = employeeRepository.findEligibleManagers(
                tenantId,
                currentEmployeeId,
                allowedCompanyIds,
                allowedLocationIds,
                allowedDepartmentIds,
                searchTerm
        );

        log.info("Found {} eligible managers for user {}", managers.size(), userId);

        // Convert to response DTOs and sort by name
        return managers.stream()
                .map(this::toManagerOptionResponse)
                .sorted(java.util.Comparator.comparing(ManagerOptionResponse::getEmployeeName))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Convert Employee entity to ManagerOptionResponse DTO
     */
    private ManagerOptionResponse toManagerOptionResponse(Employee employee) {
        return ManagerOptionResponse.builder()
                .id(employee.getId())
                .empId(employee.getEmpId())
                .employeeName(employee.getEmployeeName())
                .designation(null)  // Optional - can be enhanced with mapper
                .department(null)   // Optional - can be enhanced with mapper
                .role("MANAGER")    // Placeholder - actual role from UserAccount
                .isActive(true)     // Placeholder - actual status from UserAccount
                .build();
    }
}
