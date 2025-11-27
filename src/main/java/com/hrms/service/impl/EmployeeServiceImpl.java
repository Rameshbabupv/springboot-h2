package com.hrms.service.impl;

import com.hrms.dto.request.EmployeeRequest;
import com.hrms.dto.response.EmployeeResponse;
import com.hrms.entity.*;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.exception.ValidationException;
import com.hrms.mapper.EmployeeMapper;
import com.hrms.repository.*;
import com.hrms.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Validate 13 required fields
        validateRequiredFields(request);

        // Check for duplicate empId
        if (employeeRepository.findByTenantIdAndEmpId(request.getTenantId(), request.getEmpId()).isPresent()) {
            throw new ValidationException("Employee ID '" + request.getEmpId() + "' already exists for this tenant");
        }

        // Map DTO to entity (MapStruct handles all 78 fields automatically!)
        Employee employee = employeeMapper.toEntity(request);

        // Set required relationships manually (foreign keys)
        setRequiredRelationships(employee, request);

        // Set optional relationships
        setOptionalRelationships(employee, request);

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

        // Validate required fields
        validateRequiredFields(request);

        // Check for duplicate empId (if changed)
        if (!existingEmployee.getEmpId().equals(request.getEmpId())) {
            if (employeeRepository.findByTenantIdAndEmpId(request.getTenantId(), request.getEmpId()).isPresent()) {
                throw new ValidationException("Employee ID '" + request.getEmpId() + "' already exists");
            }
        }

        // Update entity from request (MapStruct handles all fields!)
        employeeMapper.updateEntityFromRequest(request, existingEmployee);

        // Update relationships
        setRequiredRelationships(existingEmployee, request);
        setOptionalRelationships(existingEmployee, request);

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
     */
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
}
