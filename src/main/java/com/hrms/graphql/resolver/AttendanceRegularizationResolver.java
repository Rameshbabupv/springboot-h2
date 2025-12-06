package com.hrms.graphql.resolver;

import com.hrms.entity.AttendanceRegularization;
import com.hrms.entity.Company;
import com.hrms.entity.DailyAttendance;
import com.hrms.entity.Employee;
import com.hrms.entity.UserAccount;
import com.hrms.enums.ApprovalStatus;
import com.hrms.graphql.input.RegularizationInput;
import com.hrms.service.AttendanceRegularizationService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL resolver for AttendanceRegularization operations.
 */
@Controller
public class AttendanceRegularizationResolver {

    private final AttendanceRegularizationService regularizationService;

    public AttendanceRegularizationResolver(AttendanceRegularizationService regularizationService) {
        this.regularizationService = regularizationService;
    }

    // Schema Mappings for nested objects

    @SchemaMapping(typeName = "AttendanceRegularization", field = "employee")
    public Employee employee(AttendanceRegularization regularization) {
        return regularization.getEmployee();
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
        return regularization.getApprovedBy();
    }

    // Queries

    @QueryMapping
    public List<AttendanceRegularization> regularizations(@Argument String tenantId,
                                                           @Argument Long companyId,
                                                           @Argument ApprovalStatus status,
                                                           @Argument Long employeeId) {
        return regularizationService.getRegularizations(tenantId, companyId, status, employeeId);
    }

    @QueryMapping
    public List<AttendanceRegularization> pendingRegularizations(@Argument String tenantId,
                                                                  @Argument Long companyId) {
        return regularizationService.getPendingRegularizations(tenantId, companyId);
    }

    // Mutations

    @MutationMapping
    public AttendanceRegularization submitRegularization(@Argument String tenantId,
                                                          @Argument Long companyId,
                                                          @Argument RegularizationInput input) {
        // Note: employeeId should come from authenticated user context
        // For now, it can be derived from dailyAttendanceId or passed separately
        return regularizationService.submitRegularization(tenantId, companyId, null, input);
    }

    @MutationMapping
    public AttendanceRegularization approveRegularization(@Argument String tenantId,
                                                           @Argument Long id) {
        // Note: approverId should come from authenticated user context
        return regularizationService.approveRegularization(tenantId, id, null);
    }

    @MutationMapping
    public AttendanceRegularization rejectRegularization(@Argument String tenantId,
                                                          @Argument Long id,
                                                          @Argument String reason) {
        // Note: approverId should come from authenticated user context
        return regularizationService.rejectRegularization(tenantId, id, null, reason);
    }
}
