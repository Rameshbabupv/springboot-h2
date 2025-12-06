package com.hrms.graphql.resolver;

import com.hrms.entity.Shift;
import com.hrms.graphql.input.ShiftInput;
import com.hrms.service.ShiftService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL resolver for Shift operations.
 */
@Controller
public class ShiftResolver {

    private final ShiftService shiftService;

    public ShiftResolver(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    // Queries

    @QueryMapping
    public List<Shift> shifts(@Argument String tenantId,
                              @Argument Long companyId,
                              @Argument Boolean isActive) {
        return shiftService.getShifts(tenantId, companyId, isActive);
    }

    @QueryMapping
    public Shift shift(@Argument String tenantId, @Argument Long id) {
        return shiftService.getShift(tenantId, id).orElse(null);
    }

    // Mutations

    @MutationMapping
    public Shift createShift(@Argument String tenantId,
                             @Argument Long companyId,
                             @Argument ShiftInput input) {
        return shiftService.createShift(tenantId, companyId, input);
    }

    @MutationMapping
    public Shift updateShift(@Argument String tenantId,
                             @Argument Long id,
                             @Argument ShiftInput input) {
        return shiftService.updateShift(tenantId, id, input);
    }

    @MutationMapping
    public Boolean deleteShift(@Argument String tenantId, @Argument Long id) {
        return shiftService.deleteShift(tenantId, id);
    }
}
