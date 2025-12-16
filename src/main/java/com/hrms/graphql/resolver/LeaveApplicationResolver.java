package com.hrms.graphql.resolver;

import com.hrms.entity.LeaveApplication;
import com.hrms.enums.LeaveApplicationStatus;
import com.hrms.graphql.input.LeaveApplicationInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.LeaveApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;

/**
 * GraphQL Resolver for LeaveApplication operations.
 *
 * JWT Integration:
 * - tenantId is extracted from JWT token (preferred)
 * - @Argument tenantId kept for backward compatibility during migration
 * - JWT takes precedence when available
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class LeaveApplicationResolver {

    private final LeaveApplicationService leaveApplicationService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    @QueryMapping
    public List<LeaveApplication> leaveApplications(@Argument(name = "tenantId") String tenantIdArg,
                                                     @Argument Long companyId,
                                                     @Argument Long employeeId,
                                                     @Argument LeaveApplicationStatus status,
                                                     @Argument String fromDate,
                                                     @Argument String toDate) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: leaveApplications - tenantId: {}, companyId: {}, employeeId: {}",
                  tenantId, companyId, employeeId);
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : null;
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : null;
        return leaveApplicationService.getLeaveApplications(tenantId, companyId, employeeId, status, from, to);
    }

    @QueryMapping
    public LeaveApplication leaveApplication(@Argument Long id) {
        log.debug("GraphQL Query: leaveApplication - id: {}", id);
        return leaveApplicationService.getLeaveApplicationById(id).orElse(null);
    }

    @QueryMapping
    public LeaveApplication leaveApplicationByNumber(@Argument(name = "tenantId") String tenantIdArg,
                                                      @Argument Long companyId,
                                                      @Argument String applicationNumber) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: leaveApplicationByNumber - applicationNumber: {}", applicationNumber);
        return leaveApplicationService.getLeaveApplicationByNumber(tenantId, companyId, applicationNumber).orElse(null);
    }

    @QueryMapping
    public List<LeaveApplication> pendingApprovals(@Argument(name = "tenantId") String tenantIdArg,
                                                    @Argument Long companyId,
                                                    @Argument Long approverId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: pendingApprovals - approverId: {}", approverId);
        return leaveApplicationService.getPendingApprovals(tenantId, companyId, approverId);
    }

    @QueryMapping
    public List<LeaveApplication> myLeaveApplications(@Argument(name = "tenantId") String tenantIdArg,
                                                       @Argument Long companyId,
                                                       @Argument Long employeeId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: myLeaveApplications - employeeId: {}", employeeId);
        return leaveApplicationService.getMyApplications(tenantId, companyId, employeeId);
    }

    @MutationMapping
    public LeaveApplication createLeaveApplication(@Argument(name = "tenantId") String tenantIdArg,
                                                    @Argument Long companyId,
                                                    @Argument Long employeeId,
                                                    @Argument LeaveApplicationInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Mutation: createLeaveApplication - employeeId: {}, leaveTypeId: {}",
                  employeeId, input.getLeaveTypeId());
        LeaveApplication application = mapInputToEntity(input);
        return leaveApplicationService.createLeaveApplication(tenantId, companyId, employeeId, application);
    }

    @MutationMapping
    public LeaveApplication updateLeaveApplication(@Argument Long id,
                                                    @Argument LeaveApplicationInput input) {
        log.debug("GraphQL Mutation: updateLeaveApplication - id: {}", id);
        LeaveApplication application = mapInputToEntity(input);
        return leaveApplicationService.updateLeaveApplication(id, application);
    }

    @MutationMapping
    public LeaveApplication submitLeaveApplication(@Argument Long id) {
        log.debug("GraphQL Mutation: submitLeaveApplication - id: {}", id);
        return leaveApplicationService.submitApplication(id);
    }

    @MutationMapping
    public LeaveApplication approveLeaveApplication(@Argument Long id, @Argument String remarks) {
        log.debug("GraphQL Mutation: approveLeaveApplication - id: {}", id);
        return leaveApplicationService.approveApplication(id, remarks);
    }

    @MutationMapping
    public LeaveApplication rejectLeaveApplication(@Argument Long id, @Argument String reason) {
        log.debug("GraphQL Mutation: rejectLeaveApplication - id: {}", id);
        return leaveApplicationService.rejectApplication(id, reason);
    }

    @MutationMapping
    public LeaveApplication cancelLeaveApplication(@Argument Long id, @Argument String reason) {
        log.debug("GraphQL Mutation: cancelLeaveApplication - id: {}", id);
        return leaveApplicationService.cancelApplication(id, reason);
    }

    @MutationMapping
    public LeaveApplication withdrawLeaveApplication(@Argument Long id) {
        log.debug("GraphQL Mutation: withdrawLeaveApplication - id: {}", id);
        return leaveApplicationService.withdrawApplication(id);
    }

    @MutationMapping
    public Boolean deleteLeaveApplication(@Argument Long id) {
        log.debug("GraphQL Mutation: deleteLeaveApplication - id: {}", id);
        leaveApplicationService.deleteLeaveApplication(id);
        return true;
    }

    private LeaveApplication mapInputToEntity(LeaveApplicationInput input) {
        LeaveApplication entity = new LeaveApplication();
        entity.setFromDate(LocalDate.parse(input.getFromDate()));
        entity.setToDate(LocalDate.parse(input.getToDate()));
        entity.setFromDayType(input.getFromDayType());
        entity.setToDayType(input.getToDayType());
        entity.setReason(input.getReason());
        entity.calculateTotalDays();
        return entity;
    }
}
