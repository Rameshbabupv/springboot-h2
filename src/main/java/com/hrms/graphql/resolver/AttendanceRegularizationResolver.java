package com.hrms.graphql.resolver;

import com.hrms.entity.AttendanceRegularization;
import com.hrms.entity.Company;
import com.hrms.entity.DailyAttendance;
import com.hrms.entity.Employee;
import com.hrms.entity.UserAccount;
import com.hrms.enums.ApprovalStatus;
import com.hrms.graphql.input.RegularizationInput;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.UserAccountRepository;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.AttendanceRegularizationService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * GraphQL resolver for AttendanceRegularization operations.
 */
@Controller
public class AttendanceRegularizationResolver {

    private final AttendanceRegularizationService regularizationService;
    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    public AttendanceRegularizationResolver(AttendanceRegularizationService regularizationService,
                                           EmployeeRepository employeeRepository,
                                           UserAccountRepository userAccountRepository,
                                           JwtClaimsExtractor jwtClaimsExtractor) {
        this.regularizationService = regularizationService;
        this.employeeRepository = employeeRepository;
        this.userAccountRepository = userAccountRepository;
        this.jwtClaimsExtractor = jwtClaimsExtractor;
    }

    // Schema Mappings for nested objects

    @SchemaMapping(typeName = "AttendanceRegularization", field = "employee")
    public Employee employee(AttendanceRegularization regularization) {
        if (regularization.getEmployee() == null) {
            return null;
        }
        // Fetch employee from database to avoid lazy loading issues
        return employeeRepository.findById(regularization.getEmployee().getId())
            .orElse(null);
    }


    @SchemaMapping(typeName = "AttendanceRegularization", field = "employeeId")
    public Long employeeId(AttendanceRegularization regularization) {
        return regularization.getEmployee() != null ? regularization.getEmployee().getId() : null;
    }


    @SchemaMapping(typeName = "AttendanceRegularization", field = "companyId")
    public Long companyId(AttendanceRegularization regularization) {
        return regularization.getCompany() != null ? regularization.getCompany().getId() : null;
    }

    @SchemaMapping(typeName = "AttendanceRegularization", field = "dailyAttendanceId")
    public Long dailyAttendanceId(AttendanceRegularization regularization) {
        return regularization.getDailyAttendance() != null ? regularization.getDailyAttendance().getId() : null;
    }

    @SchemaMapping(typeName = "AttendanceRegularization", field = "approvedBy")
    public Long approvedBy(AttendanceRegularization regularization) {
        return regularization.getApprovedBy() != null ? regularization.getApprovedBy().getId() : null;
    }

    @SchemaMapping(typeName = "AttendanceRegularization", field = "company")
    public Company company(AttendanceRegularization regularization) {
        return regularization.getCompany();
    }

    @SchemaMapping(typeName = "AttendanceRegularization", field = "dailyAttendance")
    public DailyAttendance dailyAttendance(AttendanceRegularization regularization) {
        return regularization.getDailyAttendance();
    }

    @SchemaMapping(typeName = "AttendanceRegularization", field = "approvedByUser")
    public UserAccount approvedByUser(AttendanceRegularization regularization) {
        if (regularization.getApprovedBy() == null) {
            return null;
        }
        // Fetch user from database to avoid lazy loading issues
        return userAccountRepository.findById(regularization.getApprovedBy().getId())
            .orElse(null);
    }

    // Queries

    @QueryMapping
    public List<AttendanceRegularization> regularizations(@Argument(name = "tenantId") String tenantIdArg,
                                                           @Argument Long companyId,
                                                           @Argument ApprovalStatus status,
                                                           @Argument Long employeeId,
                                                           @Argument String dateFrom,
                                                           @Argument String dateTo,
                                                           @Argument Long locationId,
                                                           @Argument Long departmentId,
                                                           @Argument String searchQuery,
                                                           @Argument Long userId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        // Parse date filters
        LocalDate from = dateFrom != null ? LocalDate.parse(dateFrom, DateTimeFormatter.ISO_DATE) : null;
        LocalDate to = dateTo != null ? LocalDate.parse(dateTo, DateTimeFormatter.ISO_DATE) : null;

        // If userId is provided, use role-based filtering
        // Note: userId parameter is temporary until proper authentication context is implemented
        if (userId != null) {
            return regularizationService.getRegularizationsWithRoleFilter(tenantId, companyId, status,
                    from, to, locationId, departmentId, searchQuery, userId);
        }

        // If employeeId is provided, filter by that specific employee
        if (employeeId != null) {
            return regularizationService.getRegularizationsWithFilters(tenantId, companyId, status, employeeId,
                    from, to, locationId, departmentId, searchQuery);
        }

        // Use enhanced filter method if any additional filters are provided
        if (dateFrom != null || dateTo != null || locationId != null || departmentId != null ||
            (searchQuery != null && !searchQuery.isEmpty())) {
            return regularizationService.getRegularizationsWithFilters(tenantId, companyId, status, employeeId,
                    from, to, locationId, departmentId, searchQuery);
        }

        return regularizationService.getRegularizations(tenantId, companyId, status, employeeId);
    }

    @QueryMapping
    public List<AttendanceRegularization> pendingRegularizations(@Argument(name = "tenantId") String tenantIdArg,
                                                                  @Argument Long companyId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return regularizationService.getPendingRegularizations(tenantId, companyId);
    }

    /**
     * Get employee's own regularization history (Employee Portal self-service).
     */
    @QueryMapping
    public List<AttendanceRegularization> myRegularizations(@Argument(name = "tenantId") String tenantIdArg,
                                                             @Argument Long employeeId,
                                                             @Argument ApprovalStatus status,
                                                             @Argument String dateFrom,
                                                             @Argument String dateTo) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        LocalDate from = dateFrom != null ? LocalDate.parse(dateFrom, DateTimeFormatter.ISO_DATE) : null;
        LocalDate to = dateTo != null ? LocalDate.parse(dateTo, DateTimeFormatter.ISO_DATE) : null;
        return regularizationService.getEmployeeRegularizations(tenantId, employeeId, status, from, to);
    }

    // Mutations

    @MutationMapping
    public AttendanceRegularization submitRegularization(@Argument(name = "tenantId") String tenantIdArg,
                                                          @Argument Long companyId,
                                                          @Argument RegularizationInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        // Use employeeId from input (self-service) or from authenticated user context
        Long employeeId = input.getEmployeeId();
        return regularizationService.submitRegularization(tenantId, companyId, employeeId, input);
    }

    @MutationMapping
    public AttendanceRegularization approveRegularization(@Argument(name = "tenantId") String tenantIdArg,
                                                           @Argument Long id,
                                                           @Argument String remarks) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        // Note: approverId should come from authenticated user context
        return regularizationService.approveRegularization(tenantId, id, null, remarks);
    }

    @MutationMapping
    public AttendanceRegularization rejectRegularization(@Argument(name = "tenantId") String tenantIdArg,
                                                          @Argument Long id,
                                                          @Argument String reason) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        // Note: approverId should come from authenticated user context
        return regularizationService.rejectRegularization(tenantId, id, null, reason);
    }

    @MutationMapping
    public AttendanceRegularizationService.BulkRegularizationResult bulkApproveRegularizations(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument List<Long> ids,
            @Argument String remarks) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        // Note: approverId should come from authenticated user context
        return regularizationService.bulkApproveRegularizations(tenantId, ids, null, remarks);
    }

    @MutationMapping
    public AttendanceRegularizationService.BulkRegularizationResult bulkRejectRegularizations(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument List<Long> ids,
            @Argument String reason) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        // Note: approverId should come from authenticated user context
        return regularizationService.bulkRejectRegularizations(tenantId, ids, null, reason);
    }
}
