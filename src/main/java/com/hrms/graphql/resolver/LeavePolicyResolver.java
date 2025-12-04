package com.hrms.graphql.resolver;

import com.hrms.entity.LeavePolicy;
import com.hrms.graphql.input.LeavePolicyInput;
import com.hrms.service.LeavePolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Resolver for LeavePolicy operations.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class LeavePolicyResolver {

    private final LeavePolicyService leavePolicyService;

    @QueryMapping
    public List<LeavePolicy> leavePolicies(@Argument String tenantId,
                                            @Argument Long companyId,
                                            @Argument String status) {
        log.debug("GraphQL Query: leavePolicies - tenantId: {}, companyId: {}, status: {}",
                  tenantId, companyId, status);
        return leavePolicyService.getLeavePolicies(tenantId, companyId, status);
    }

    @QueryMapping
    public LeavePolicy leavePolicy(@Argument Long id) {
        log.debug("GraphQL Query: leavePolicy - id: {}", id);
        return leavePolicyService.getLeavePolicyById(id).orElse(null);
    }

    @QueryMapping
    public LeavePolicy leavePolicyWithEntitlements(@Argument Long id) {
        log.debug("GraphQL Query: leavePolicyWithEntitlements - id: {}", id);
        return leavePolicyService.getLeavePolicyWithEntitlements(id).orElse(null);
    }

    @QueryMapping
    public LeavePolicy defaultLeavePolicy(@Argument String tenantId, @Argument Long companyId) {
        log.debug("GraphQL Query: defaultLeavePolicy - tenantId: {}, companyId: {}", tenantId, companyId);
        return leavePolicyService.getDefaultLeavePolicy(tenantId, companyId).orElse(null);
    }

    @SchemaMapping(typeName = "LeavePolicy", field = "employeeCount")
    public Integer employeeCount(LeavePolicy policy) {
        Long count = leavePolicyService.countEmployeesByPolicy(policy.getId());
        return count != null ? count.intValue() : 0;
    }

    @MutationMapping
    public LeavePolicy createLeavePolicy(@Argument String tenantId,
                                          @Argument Long companyId,
                                          @Argument LeavePolicyInput input) {
        log.debug("GraphQL Mutation: createLeavePolicy - tenantId: {}, companyId: {}, code: {}",
                  tenantId, companyId, input.getCode());
        LeavePolicy policy = mapInputToEntity(input);
        return leavePolicyService.createLeavePolicy(tenantId, companyId, policy);
    }

    @MutationMapping
    public LeavePolicy updateLeavePolicy(@Argument Long id, @Argument LeavePolicyInput input) {
        log.debug("GraphQL Mutation: updateLeavePolicy - id: {}", id);
        LeavePolicy policy = mapInputToEntity(input);
        return leavePolicyService.updateLeavePolicy(id, policy);
    }

    @MutationMapping
    public Boolean deleteLeavePolicy(@Argument Long id) {
        log.debug("GraphQL Mutation: deleteLeavePolicy - id: {}", id);
        leavePolicyService.deleteLeavePolicy(id);
        return true;
    }

    @MutationMapping
    public LeavePolicy activateLeavePolicy(@Argument Long id) {
        log.debug("GraphQL Mutation: activateLeavePolicy - id: {}", id);
        return leavePolicyService.activatePolicy(id);
    }

    @MutationMapping
    public LeavePolicy deactivateLeavePolicy(@Argument Long id) {
        log.debug("GraphQL Mutation: deactivateLeavePolicy - id: {}", id);
        return leavePolicyService.deactivatePolicy(id);
    }

    @MutationMapping
    public LeavePolicy setDefaultLeavePolicy(@Argument Long id) {
        log.debug("GraphQL Mutation: setDefaultLeavePolicy - id: {}", id);
        return leavePolicyService.setDefaultPolicy(id);
    }

    private LeavePolicy mapInputToEntity(LeavePolicyInput input) {
        LeavePolicy entity = new LeavePolicy();
        entity.setCode(input.getCode());
        entity.setName(input.getName());
        entity.setDescription(input.getDescription());
        entity.setLeaveYearStart(input.getLeaveYearStart() != null ? input.getLeaveYearStart() : "JANUARY");
        entity.setStatus(input.getStatus() != null ? input.getStatus() : "DRAFT");
        entity.setIsDefault(input.getIsDefault() != null ? input.getIsDefault() : false);
        return entity;
    }
}
