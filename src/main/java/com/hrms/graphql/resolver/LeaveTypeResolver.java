package com.hrms.graphql.resolver;

import com.hrms.entity.LeaveType;
import com.hrms.graphql.input.LeaveTypeInput;
import com.hrms.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Resolver for LeaveType operations.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class LeaveTypeResolver {

    private final LeaveTypeService leaveTypeService;

    @QueryMapping
    public List<LeaveType> leaveTypes(@Argument String tenantId,
                                       @Argument Long companyId,
                                       @Argument Boolean activeOnly) {
        log.debug("GraphQL Query: leaveTypes - tenantId: {}, companyId: {}, activeOnly: {}",
                  tenantId, companyId, activeOnly);
        return leaveTypeService.getLeaveTypes(tenantId, companyId, activeOnly);
    }

    @QueryMapping
    public LeaveType leaveType(@Argument Long id) {
        log.debug("GraphQL Query: leaveType - id: {}", id);
        return leaveTypeService.getLeaveTypeById(id).orElse(null);
    }

    @QueryMapping
    public LeaveType leaveTypeByCode(@Argument String tenantId,
                                      @Argument Long companyId,
                                      @Argument String code) {
        log.debug("GraphQL Query: leaveTypeByCode - tenantId: {}, companyId: {}, code: {}",
                  tenantId, companyId, code);
        return leaveTypeService.getLeaveTypeByCode(tenantId, companyId, code).orElse(null);
    }

    @QueryMapping
    public List<LeaveType> sharedLeaveTypes(@Argument String tenantId) {
        log.debug("GraphQL Query: sharedLeaveTypes - tenantId: {}", tenantId);
        return leaveTypeService.getSharedLeaveTypes(tenantId);
    }

    @MutationMapping
    public LeaveType createLeaveType(@Argument String tenantId,
                                      @Argument Long companyId,
                                      @Argument LeaveTypeInput input) {
        log.debug("GraphQL Mutation: createLeaveType - tenantId: {}, companyId: {}, code: {}",
                  tenantId, companyId, input.getCode());
        LeaveType leaveType = mapInputToEntity(input);
        return leaveTypeService.createLeaveType(tenantId, companyId, leaveType);
    }

    @MutationMapping
    public LeaveType updateLeaveType(@Argument Long id, @Argument LeaveTypeInput input) {
        log.debug("GraphQL Mutation: updateLeaveType - id: {}", id);
        LeaveType leaveType = mapInputToEntity(input);
        return leaveTypeService.updateLeaveType(id, leaveType);
    }

    @MutationMapping
    public Boolean deleteLeaveType(@Argument Long id) {
        log.debug("GraphQL Mutation: deleteLeaveType - id: {}", id);
        leaveTypeService.deleteLeaveType(id);
        return true;
    }

    @MutationMapping
    public LeaveType toggleLeaveTypeStatus(@Argument Long id) {
        log.debug("GraphQL Mutation: toggleLeaveTypeStatus - id: {}", id);
        return leaveTypeService.toggleStatus(id);
    }

    private LeaveType mapInputToEntity(LeaveTypeInput input) {
        LeaveType entity = new LeaveType();
        entity.setCode(input.getCode());
        entity.setName(input.getName());
        entity.setCategory(input.getCategory());
        entity.setDescription(input.getDescription());
        entity.setIcon(input.getIcon());
        entity.setColor(input.getColor());
        entity.setIsActive(input.getIsActive() != null ? input.getIsActive() : true);
        return entity;
    }
}
