package com.hrms.graphql.resolver;

import com.hrms.dto.request.EmployeeFilterCriteria;
import com.hrms.dto.request.EmployeeRequest;
import com.hrms.dto.response.EmployeePageResponse;
import com.hrms.dto.response.EmployeeResponse;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.EmployeeService;
import com.hrms.service.OrganizationalScopeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Resolver for Employee Operations
 * PRIMARY API for employee data access (REST is only for auth)
 *
 * JWT Integration:
 * - tenantId is extracted from JWT token (preferred)
 * - @Argument tenantId kept for backward compatibility during migration
 * - JWT takes precedence when available
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class EmployeeResolver {

    private final EmployeeService employeeService;
    private final OrganizationalScopeService organizationalScopeService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    // =====================================================
    // QUERY OPERATIONS
    // =====================================================

    @QueryMapping
    public EmployeeResponse employeeById(@Argument(name = "tenantId") String tenantIdArg, @Argument String id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: employeeById - tenantId: {}, id: {}", tenantId, id);
        EmployeeResponse result = employeeService.getEmployeeById(tenantId, Long.parseLong(id));
        log.info("GraphQL Response: employeeById - returned employee: {}", result.getEmployeeName());
        return result;
    }

    @QueryMapping
    public EmployeeResponse employeeByEmpId(@Argument(name = "tenantId") String tenantIdArg, @Argument String empId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: employeeByEmpId - tenantId: {}, empId: {}", tenantId, empId);
        EmployeeResponse result = employeeService.getEmployeeByEmpId(tenantId, empId);
        log.info("GraphQL Response: employeeByEmpId - returned employee: {}", result.getEmployeeName());
        return result;
    }

    @QueryMapping
    public List<EmployeeResponse> employeesByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: employeesByTenant - tenantId: {}", tenantId);
        List<EmployeeResponse> result = employeeService.getAllEmployeesByTenant(tenantId);
        log.info("GraphQL Response: employeesByTenant - returned {} employees for tenant: {}", result.size(), tenantId);
        return result;
    }

    @QueryMapping
    public List<EmployeeResponse> employeesByCompany(@Argument(name = "tenantId") String tenantIdArg, @Argument String companyId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: employeesByCompany - tenantId: {}, companyId: {}", tenantId, companyId);
        List<EmployeeResponse> result = employeeService.getEmployeesByCompany(tenantId, Long.parseLong(companyId));
        log.info("GraphQL Response: employeesByCompany - returned {} employees for company: {}", result.size(), companyId);
        return result;
    }

    @QueryMapping
    public List<EmployeeResponse> employeesByDepartment(@Argument(name = "tenantId") String tenantIdArg, @Argument String departmentId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: employeesByDepartment - tenantId: {}, departmentId: {}", tenantId, departmentId);
        List<EmployeeResponse> result = employeeService.getEmployeesByDepartment(tenantId, Long.parseLong(departmentId));
        log.info("GraphQL Response: employeesByDepartment - returned {} employees for department: {}", result.size(), departmentId);
        return result;
    }

    @QueryMapping
    public List<EmployeeResponse> employeesByDesignation(@Argument(name = "tenantId") String tenantIdArg, @Argument String designationId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: employeesByDesignation - tenantId: {}, designationId: {}", tenantId, designationId);
        List<EmployeeResponse> result = employeeService.getEmployeesByDesignation(tenantId, Long.parseLong(designationId));
        log.info("GraphQL Response: employeesByDesignation - returned {} employees for designation: {}", result.size(), designationId);
        return result;
    }

    @QueryMapping
    public List<EmployeeResponse> employeesByStatus(@Argument(name = "tenantId") String tenantIdArg, @Argument String status) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: employeesByStatus - tenantId: {}, status: {}", tenantId, status);
        List<EmployeeResponse> result = employeeService.getEmployeesByStatus(tenantId, status);
        log.info("GraphQL Response: employeesByStatus - returned {} employees with status: {}", result.size(), status);
        return result;
    }

    @QueryMapping
    public List<EmployeeResponse> employeesByReportingManager(@Argument(name = "tenantId") String tenantIdArg, @Argument String managerId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: employeesByReportingManager - tenantId: {}, managerId: {}", tenantId, managerId);
        List<EmployeeResponse> result = employeeService.getEmployeesByReportingManager(tenantId, Long.parseLong(managerId));
        log.info("GraphQL Response: employeesByReportingManager - returned {} employees for manager: {}", result.size(), managerId);
        return result;
    }

    @QueryMapping
    public List<EmployeeResponse> searchEmployees(@Argument(name = "tenantId") String tenantIdArg, @Argument String searchTerm) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: searchEmployees - tenantId: {}, searchTerm: {}", tenantId, searchTerm);
        List<EmployeeResponse> result = employeeService.searchEmployees(tenantId, searchTerm);
        log.info("GraphQL Response: searchEmployees - returned {} employees matching: {}", result.size(), searchTerm);
        return result;
    }

    @QueryMapping
    public Long employeeCount(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: employeeCount - tenantId: {}", tenantId);
        Long result = employeeService.getEmployeeCountByTenant(tenantId);
        log.info("GraphQL Response: employeeCount - total count: {} for tenant: {}", result, tenantId);
        return result;
    }

    @QueryMapping
    public Long employeeCountByStatus(@Argument(name = "tenantId") String tenantIdArg, @Argument String status) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: employeeCountByStatus - tenantId: {}, status: {}", tenantId, status);
        Long result = employeeService.getEmployeeCountByStatus(tenantId, status);
        log.info("GraphQL Response: employeeCountByStatus - count: {} for status: {}", result, status);
        return result;
    }

    // =====================================================
    // NEW: ADVANCED FILTERED QUERIES WITH SECURITY
    // =====================================================

    /**
     * Get filtered employees with organizational scope security
     * This is the PRIMARY method for employee listing with security enforcement
     *
     * 4-STEP SECURITY PROCESS:
     * 1. Get user's organizational scope from DB
     * 2. Validate user can access requested filters
     * 3. Merge requested filters with authorized scope (intersection)
     * 4. Execute filtered query with security applied
     */
    @QueryMapping
    public EmployeePageResponse filteredEmployees(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument String userId,
            @Argument List<String> companyIds,
            @Argument List<String> locationIds,
            @Argument List<String> divisionIds,
            @Argument List<String> departmentIds,
            @Argument List<String> sectionIds,
            @Argument List<String> designationIds,
            @Argument List<String> gradeIds,
            @Argument List<String> jobFunctionIds,
            @Argument List<String> employmentTypeIds,
            @Argument String searchQuery,
            @Argument String employeeStatus,
            @Argument String reportingManagerId,
            @Argument Integer page,
            @Argument Integer size,
            @Argument String sortBy,
            @Argument String sortDirection) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.debug("GraphQL Query: filteredEmployees - tenantId: {}, userId: {}", tenantId, userId);

        // Build filter criteria from arguments
        EmployeeFilterCriteria criteria = EmployeeFilterCriteria.builder()
                .tenantId(tenantId)
                .companyIds(convertToLongList(companyIds))
                .locationIds(convertToLongList(locationIds))
                .divisionIds(convertToLongList(divisionIds))
                .departmentIds(convertToLongList(departmentIds))
                .sectionIds(convertToLongList(sectionIds))
                .designationIds(convertToLongList(designationIds))
                .gradeIds(convertToLongList(gradeIds))
                .jobFunctionIds(convertToLongList(jobFunctionIds))
                .employmentTypeIds(convertToLongList(employmentTypeIds))
                .searchQuery(searchQuery)
                .employeeStatus(employeeStatus)
                .reportingManagerId(reportingManagerId != null ? Long.parseLong(reportingManagerId) : null)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        // STEP 1-3: Apply security scope (validates and merges filters)
        criteria = organizationalScopeService.applySecurityScope(tenantId, Long.parseLong(userId), criteria);

        // STEP 4: Execute filtered query with security applied
        Page<EmployeeResponse> employeePage = employeeService.getFilteredEmployees(criteria);

        // Convert Spring Page to GraphQL response
        EmployeePageResponse response = EmployeePageResponse.builder()
                .content(employeePage.getContent())
                .totalElements(employeePage.getTotalElements())
                .totalPages(employeePage.getTotalPages())
                .currentPage(employeePage.getNumber())
                .pageSize(employeePage.getSize())
                .hasNext(employeePage.hasNext())
                .hasPrevious(employeePage.hasPrevious())
                .build();

        log.info("GraphQL Response: filteredEmployees - returned {} employees (page {}/{}, total: {})",
                 response.getContent().size(), response.getCurrentPage() + 1, response.getTotalPages(), response.getTotalElements());
        return response;
    }

    /**
     * Count filtered employees with organizational scope security
     */
    @QueryMapping
    public Long filteredEmployeesCount(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument String userId,
            @Argument List<String> companyIds,
            @Argument List<String> locationIds,
            @Argument List<String> divisionIds,
            @Argument List<String> departmentIds,
            @Argument List<String> sectionIds,
            @Argument List<String> designationIds,
            @Argument List<String> gradeIds,
            @Argument List<String> jobFunctionIds,
            @Argument List<String> employmentTypeIds,
            @Argument String searchQuery,
            @Argument String employeeStatus,
            @Argument String reportingManagerId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.debug("GraphQL Query: filteredEmployeesCount - tenantId: {}, userId: {}", tenantId, userId);

        // Build filter criteria
        EmployeeFilterCriteria criteria = EmployeeFilterCriteria.builder()
                .tenantId(tenantId)
                .companyIds(convertToLongList(companyIds))
                .locationIds(convertToLongList(locationIds))
                .divisionIds(convertToLongList(divisionIds))
                .departmentIds(convertToLongList(departmentIds))
                .sectionIds(convertToLongList(sectionIds))
                .designationIds(convertToLongList(designationIds))
                .gradeIds(convertToLongList(gradeIds))
                .jobFunctionIds(convertToLongList(jobFunctionIds))
                .employmentTypeIds(convertToLongList(employmentTypeIds))
                .searchQuery(searchQuery)
                .employeeStatus(employeeStatus)
                .reportingManagerId(reportingManagerId != null ? Long.parseLong(reportingManagerId) : null)
                .build();

        // Apply security scope
        criteria = organizationalScopeService.applySecurityScope(tenantId, Long.parseLong(userId), criteria);

        // Execute count query
        Long result = employeeService.countFilteredEmployees(criteria);
        log.info("GraphQL Response: filteredEmployeesCount - total count: {} for tenant: {}", result, tenantId);
        return result;
    }

    /**
     * Helper method to convert String list to Long list
     */
    private List<Long> convertToLongList(List<String> stringList) {
        if (stringList == null || stringList.isEmpty()) {
            return null;
        }
        return stringList.stream()
                .map(Long::parseLong)
                .toList();
    }

    // =====================================================
    // MUTATION OPERATIONS
    // =====================================================

    @MutationMapping
    public EmployeeResponse createEmployee(@Argument EmployeeRequest input) {
        log.debug("GraphQL Mutation: createEmployee - empId: {}, tenantId: {}", input.getEmpId(), input.getTenantId());
        EmployeeResponse result = employeeService.createEmployee(input);
        log.info("GraphQL Response: createEmployee - created employee id: {}, empId: {}, name: {}",
                 result.getId(), result.getEmpId(), result.getEmployeeName());
        return result;
    }

    @MutationMapping
    public EmployeeResponse updateEmployee(@Argument(name = "tenantId") String tenantIdArg, @Argument String id, @Argument EmployeeRequest input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Mutation: updateEmployee - id: {}, tenantId: {}", id, tenantId);
        EmployeeResponse result = employeeService.updateEmployee(tenantId, Long.parseLong(id), input);
        log.info("GraphQL Response: updateEmployee - updated employee id: {}, empId: {}",
                 result.getId(), result.getEmpId());
        return result;
    }

    @MutationMapping
    public Boolean deleteEmployee(@Argument(name = "tenantId") String tenantIdArg, @Argument String id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Mutation: deleteEmployee - id: {}, tenantId: {}", id, tenantId);
        employeeService.deleteEmployee(tenantId, Long.parseLong(id));
        log.info("GraphQL Response: deleteEmployee - successfully deleted employee id: {}", id);
        return true;
    }
}
