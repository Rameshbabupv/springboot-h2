package com.hrms.graphql.resolver;

import com.hrms.entity.Shift;
import com.hrms.enums.ShiftType;
import com.hrms.graphql.input.ShiftInput;
import com.hrms.security.JwtClaimsExtractor;
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
    private final JwtClaimsExtractor jwtClaimsExtractor;

    public ShiftResolver(ShiftService shiftService, JwtClaimsExtractor jwtClaimsExtractor) {
        this.shiftService = shiftService;
        this.jwtClaimsExtractor = jwtClaimsExtractor;
    }

    // Queries

    @QueryMapping
    public List<Shift> shifts(@Argument(name = "tenantId") String tenantIdArg,
                              @Argument Long companyId,
                              @Argument Boolean isActive,
                              @Argument ShiftType shiftType,
                              @Argument String searchQuery) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return shiftService.getShifts(tenantId, companyId, isActive, shiftType, searchQuery);
    }

    @QueryMapping
    public Shift shift(@Argument(name = "tenantId") String tenantIdArg, @Argument Long id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return shiftService.getShift(tenantId, id).orElse(null);
    }

    // Mutations

    @MutationMapping
    public Shift createShift(@Argument(name = "tenantId") String tenantIdArg,
                             @Argument Long companyId,
                             @Argument ShiftInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return shiftService.createShift(tenantId, companyId, input);
    }

    @MutationMapping
    public Shift updateShift(@Argument(name = "tenantId") String tenantIdArg,
                             @Argument Long id,
                             @Argument ShiftInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return shiftService.updateShift(tenantId, id, input);
    }

    @MutationMapping
    public Boolean deleteShift(@Argument(name = "tenantId") String tenantIdArg, @Argument Long id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return shiftService.deleteShift(tenantId, id);
    }
}
