package com.hrms.service.impl;

import com.hrms.dto.request.EmployeeRequest;
import com.hrms.entity.Company;
import com.hrms.entity.Department;
import com.hrms.entity.Designation;
import com.hrms.entity.Employee;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.DesignationRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for Employee operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;

    @Override
    public List<Employee> getAllEmployees() {
        log.debug("Fetching all employees");
        return employeeRepository.findAll();
    }

    @Override
    public Employee getEmployeeById(Long id) {
        log.debug("Fetching employee with id: {}", id);
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    @Override
    public List<Employee> getEmployeesByTenant(String tenantId) {
        log.debug("Fetching employees for tenant: {}", tenantId);
        return employeeRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Employee> getEmployeesByCompany(Long companyId) {
        log.debug("Fetching employees for company: {}", companyId);
        return employeeRepository.findByCompanyId(companyId);
    }

    @Override
    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        log.debug("Fetching employees for department: {}", departmentId);
        return employeeRepository.findByDepartmentId(departmentId);
    }

    @Override
    public List<Employee> getActiveEmployees() {
        log.debug("Fetching active employees");
        return employeeRepository.findByIsActiveTrue();
    }

    @Override
    public List<Employee> getEmployeesByStatus(String status) {
        log.debug("Fetching employees with status: {}", status);
        return employeeRepository.findByEmployeeStatus(status);
    }

    @Override
    @Transactional
    public Employee createEmployee(EmployeeRequest request) {
        log.debug("Creating new employee: {}", request.getEmployeeName());

        Employee employee = mapToEntity(request);
        Employee saved = employeeRepository.save(employee);

        log.info("Created employee with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Employee updateEmployee(Long id, EmployeeRequest request) {
        log.debug("Updating employee with id: {}", id);

        Employee existing = getEmployeeById(id);
        updateEntityFromRequest(existing, request);
        Employee updated = employeeRepository.save(existing);

        log.info("Updated employee with id: {}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        log.debug("Deleting employee with id: {}", id);

        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", "id", id);
        }
        employeeRepository.deleteById(id);

        log.info("Deleted employee with id: {}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return employeeRepository.existsById(id);
    }

    private Employee mapToEntity(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setTenantId(request.getTenantId());

        // Set company
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));
        employee.setCompany(company);

        employee.setEmpId(request.getEmpId());
        employee.setEmployeeName(request.getEmployeeName());
        employee.setGender(request.getGender());

        if (request.getDateOfBirth() != null) {
            employee.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        }
        employee.setDateOfJoin(LocalDate.parse(request.getDateOfJoin()));

        employee.setMobileNo(request.getMobileNo());
        employee.setEmailId(request.getEmailId());
        employee.setBloodGroup(request.getBloodGroup());
        employee.setMaritalStatus(request.getMaritalStatus());

        // Set department if provided
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            employee.setDepartment(department);
        }

        // Set designation if provided
        if (request.getDesignationId() != null) {
            Designation designation = designationRepository.findById(request.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", request.getDesignationId()));
            employee.setDesignation(designation);
        }

        // Set reporting manager if provided
        if (request.getReportingManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getReportingManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getReportingManagerId()));
            employee.setReportingManager(manager);
        }

        employee.setBasicSalary(request.getBasicSalary());
        employee.setGrossSalary(request.getGrossSalary());
        employee.setCtc(request.getCtc());
        employee.setAadharNo(request.getAadharNo());
        employee.setPanNo(request.getPanNo());
        employee.setUan(request.getUan());
        employee.setCoverPf(request.getCoverPf() != null ? request.getCoverPf() : false);
        employee.setPfNumber(request.getPfNumber());
        employee.setCoverEsi(request.getCoverEsi() != null ? request.getCoverEsi() : false);
        employee.setEsiNumber(request.getEsiNumber());
        employee.setEmployeeStatus(request.getEmployeeStatus());
        employee.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return employee;
    }

    private void updateEntityFromRequest(Employee employee, EmployeeRequest request) {
        employee.setTenantId(request.getTenantId());

        // Update company
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));
        employee.setCompany(company);

        employee.setEmpId(request.getEmpId());
        employee.setEmployeeName(request.getEmployeeName());
        employee.setGender(request.getGender());

        if (request.getDateOfBirth() != null) {
            employee.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        }
        employee.setDateOfJoin(LocalDate.parse(request.getDateOfJoin()));

        employee.setMobileNo(request.getMobileNo());
        employee.setEmailId(request.getEmailId());
        employee.setBloodGroup(request.getBloodGroup());
        employee.setMaritalStatus(request.getMaritalStatus());

        // Update department
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            employee.setDepartment(department);
        } else {
            employee.setDepartment(null);
        }

        // Update designation
        if (request.getDesignationId() != null) {
            Designation designation = designationRepository.findById(request.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", request.getDesignationId()));
            employee.setDesignation(designation);
        } else {
            employee.setDesignation(null);
        }

        // Update reporting manager
        if (request.getReportingManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getReportingManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getReportingManagerId()));
            employee.setReportingManager(manager);
        } else {
            employee.setReportingManager(null);
        }

        employee.setBasicSalary(request.getBasicSalary());
        employee.setGrossSalary(request.getGrossSalary());
        employee.setCtc(request.getCtc());
        employee.setAadharNo(request.getAadharNo());
        employee.setPanNo(request.getPanNo());
        employee.setUan(request.getUan());
        employee.setCoverPf(request.getCoverPf() != null ? request.getCoverPf() : false);
        employee.setPfNumber(request.getPfNumber());
        employee.setCoverEsi(request.getCoverEsi() != null ? request.getCoverEsi() : false);
        employee.setEsiNumber(request.getEsiNumber());
        employee.setEmployeeStatus(request.getEmployeeStatus());
        if (request.getIsActive() != null) {
            employee.setIsActive(request.getIsActive());
        }
    }
}
