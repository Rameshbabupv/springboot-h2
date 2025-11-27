package com.hrms.graphql.resolver;

import com.hrms.dto.request.EmployeeRequest;
import com.hrms.dto.response.EmployeeResponse;
import com.hrms.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Resolver for Employee Operations
 * PRIMARY API for employee data access (REST is only for auth)
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class EmployeeResolver {

    private final EmployeeService employeeService;

    // =====================================================
    // QUERY OPERATIONS
    // =====================================================

    @QueryMapping
    public EmployeeResponse employeeById(@Argument String tenantId, @Argument String id) {
        log.debug("GraphQL Query: employeeById - tenantId: {}, id: {}", tenantId, id);
        return employeeService.getEmployeeById(tenantId, Long.parseLong(id));
    }

    @QueryMapping
    public EmployeeResponse employeeByEmpId(@Argument String tenantId, @Argument String empId) {
        log.debug("GraphQL Query: employeeByEmpId - tenantId: {}, empId: {}", tenantId, empId);
        return employeeService.getEmployeeByEmpId(tenantId, empId);
    }

    @QueryMapping
    public List<EmployeeResponse> employeesByTenant(@Argument String tenantId) {
        log.debug("GraphQL Query: employeesByTenant - tenantId: {}", tenantId);
        return employeeService.getAllEmployeesByTenant(tenantId);
    }

    @QueryMapping
    public List<EmployeeResponse> employeesByCompany(@Argument String tenantId, @Argument String companyId) {
        log.debug("GraphQL Query: employeesByCompany - tenantId: {}, companyId: {}", tenantId, companyId);
        return employeeService.getEmployeesByCompany(tenantId, Long.parseLong(companyId));
    }

    @QueryMapping
    public List<EmployeeResponse> employeesByDepartment(@Argument String tenantId, @Argument String departmentId) {
        log.debug("GraphQL Query: employeesByDepartment - tenantId: {}, departmentId: {}", tenantId, departmentId);
        return employeeService.getEmployeesByDepartment(tenantId, Long.parseLong(departmentId));
    }

    @QueryMapping
    public List<EmployeeResponse> employeesByDesignation(@Argument String tenantId, @Argument String designationId) {
        log.debug("GraphQL Query: employeesByDesignation - tenantId: {}, designationId: {}", tenantId, designationId);
        return employeeService.getEmployeesByDesignation(tenantId, Long.parseLong(designationId));
    }

    @QueryMapping
    public List<EmployeeResponse> employeesByStatus(@Argument String tenantId, @Argument String status) {
        log.debug("GraphQL Query: employeesByStatus - tenantId: {}, status: {}", tenantId, status);
        return employeeService.getEmployeesByStatus(tenantId, status);
    }

    @QueryMapping
    public List<EmployeeResponse> employeesByReportingManager(@Argument String tenantId, @Argument String managerId) {
        log.debug("GraphQL Query: employeesByReportingManager - tenantId: {}, managerId: {}", tenantId, managerId);
        return employeeService.getEmployeesByReportingManager(tenantId, Long.parseLong(managerId));
    }

    @QueryMapping
    public List<EmployeeResponse> searchEmployees(@Argument String tenantId, @Argument String searchTerm) {
        log.debug("GraphQL Query: searchEmployees - tenantId: {}, searchTerm: {}", tenantId, searchTerm);
        return employeeService.searchEmployees(tenantId, searchTerm);
    }

    @QueryMapping
    public Long employeeCount(@Argument String tenantId) {
        log.debug("GraphQL Query: employeeCount - tenantId: {}", tenantId);
        return employeeService.getEmployeeCountByTenant(tenantId);
    }

    @QueryMapping
    public Long employeeCountByStatus(@Argument String tenantId, @Argument String status) {
        log.debug("GraphQL Query: employeeCountByStatus - tenantId: {}, status: {}", tenantId, status);
        return employeeService.getEmployeeCountByStatus(tenantId, status);
    }

    // =====================================================
    // MUTATION OPERATIONS
    // =====================================================

    @MutationMapping
    public EmployeeResponse createEmployee(@Argument EmployeeRequest input) {
        log.debug("GraphQL Mutation: createEmployee - empId: {}, tenantId: {}", input.getEmpId(), input.getTenantId());
        return employeeService.createEmployee(input);
    }

    @MutationMapping
    public EmployeeResponse updateEmployee(@Argument String tenantId, @Argument String id, @Argument EmployeeRequest input) {
        log.debug("GraphQL Mutation: updateEmployee - id: {}, tenantId: {}", id, tenantId);
        return employeeService.updateEmployee(tenantId, Long.parseLong(id), input);
    }

    @MutationMapping
    public Boolean deleteEmployee(@Argument String tenantId, @Argument String id) {
        log.debug("GraphQL Mutation: deleteEmployee - id: {}, tenantId: {}", id, tenantId);
        employeeService.deleteEmployee(tenantId, Long.parseLong(id));
        return true;
    }
}
