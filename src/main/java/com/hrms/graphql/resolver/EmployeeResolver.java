package com.hrms.graphql.resolver;

import com.hrms.entity.Company;
import com.hrms.entity.Department;
import com.hrms.entity.Designation;
import com.hrms.entity.Employee;
import com.hrms.graphql.input.EmployeeInput;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.DesignationRepository;
import com.hrms.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class EmployeeResolver {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;

    @QueryMapping
    public List<Employee> employees() {
        return employeeRepository.findAll();
    }

    @QueryMapping
    public Employee employee(@Argument Long id) {
        return employeeRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Employee> employeesByTenant(@Argument String tenantId) {
        return employeeRepository.findByTenantId(tenantId);
    }

    @QueryMapping
    public List<Employee> employeesByCompany(@Argument Long companyId) {
        return employeeRepository.findByCompanyId(companyId);
    }

    @QueryMapping
    public List<Employee> employeesByDepartment(@Argument Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId);
    }

    @QueryMapping
    public List<Employee> activeEmployees() {
        return employeeRepository.findByIsActiveTrue();
    }

    @QueryMapping
    public List<Employee> employeesByStatus(@Argument String status) {
        return employeeRepository.findByEmployeeStatus(status);
    }

    @MutationMapping
    public Employee createEmployee(@Argument EmployeeInput input) {
        Employee employee = mapToEntity(input);
        return employeeRepository.save(employee);
    }

    @MutationMapping
    public Employee updateEmployee(@Argument Long id, @Argument EmployeeInput input) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        updateEntityFromInput(employee, input);
        return employeeRepository.save(employee);
    }

    @MutationMapping
    public Boolean deleteEmployee(@Argument Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Employee mapToEntity(EmployeeInput input) {
        Employee employee = new Employee();
        updateEntityFromInput(employee, input);
        return employee;
    }

    private void updateEntityFromInput(Employee employee, EmployeeInput input) {
        employee.setTenantId(input.getTenantId());

        if (input.getCompanyId() != null) {
            Company company = companyRepository.findById(input.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            employee.setCompany(company);
        }

        employee.setEmpId(input.getEmpId());
        employee.setEmployeeName(input.getEmployeeName());
        employee.setGender(input.getGender());

        if (input.getDateOfBirth() != null && !input.getDateOfBirth().isEmpty()) {
            employee.setDateOfBirth(LocalDate.parse(input.getDateOfBirth()));
        }
        if (input.getDateOfJoin() != null && !input.getDateOfJoin().isEmpty()) {
            employee.setDateOfJoin(LocalDate.parse(input.getDateOfJoin()));
        }

        employee.setMobileNo(input.getMobileNo());
        employee.setEmailId(input.getEmailId());
        employee.setBloodGroup(input.getBloodGroup());
        employee.setMaritalStatus(input.getMaritalStatus());

        if (input.getDepartmentId() != null) {
            Department department = departmentRepository.findById(input.getDepartmentId())
                    .orElse(null);
            employee.setDepartment(department);
        }

        if (input.getDesignationId() != null) {
            Designation designation = designationRepository.findById(input.getDesignationId())
                    .orElse(null);
            employee.setDesignation(designation);
        }

        if (input.getReportingManagerId() != null) {
            Employee manager = employeeRepository.findById(input.getReportingManagerId())
                    .orElse(null);
            employee.setReportingManager(manager);
        }

        employee.setBasicSalary(input.getBasicSalary());
        employee.setGrossSalary(input.getGrossSalary());
        employee.setCtc(input.getCtc());
        employee.setAadharNo(input.getAadharNo());
        employee.setPanNo(input.getPanNo());
        employee.setUan(input.getUan());
        employee.setCoverPf(input.getCoverPf());
        employee.setPfNumber(input.getPfNumber());
        employee.setCoverEsi(input.getCoverEsi());
        employee.setEsiNumber(input.getEsiNumber());
        employee.setEmployeeStatus(input.getEmployeeStatus());
        employee.setIsActive(input.getIsActive());
    }
}
