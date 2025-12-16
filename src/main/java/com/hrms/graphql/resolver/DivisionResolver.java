package com.hrms.graphql.resolver;

import com.hrms.dto.request.DivisionRequest;
import com.hrms.entity.Division;
import com.hrms.graphql.input.DivisionInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.DivisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DivisionResolver {

    private final DivisionService divisionService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    @QueryMapping
    public List<Division> divisions() {
        return divisionService.getAllDivisions();
    }

    @QueryMapping
    public Division division(@Argument Long id) {
        return divisionService.getDivisionById(id);
    }

    @QueryMapping
    public List<Division> divisionsByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return divisionService.getDivisionsByTenant(tenantId);
    }

    @QueryMapping
    public List<Division> activeDivisionsByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return divisionService.getActiveDivisionsByTenant(tenantId);
    }

    @QueryMapping
    public List<Division> activeDivisions() {
        return divisionService.getActiveDivisions();
    }

    @QueryMapping
    public List<Division> searchDivisions(@Argument(name = "tenantId") String tenantIdArg, @Argument String searchTerm) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return divisionService.searchDivisions(tenantId, searchTerm);
    }

    @MutationMapping
    public Division createDivision(@Argument DivisionInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);

        DivisionRequest request = mapToRequest(input, tenantId);
        return divisionService.createDivision(request);
    }

    @MutationMapping
    public Division updateDivision(@Argument Long id, @Argument DivisionInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);

        DivisionRequest request = mapToRequest(input, tenantId);
        return divisionService.updateDivision(id, request);
    }

    @MutationMapping
    public Boolean deleteDivision(@Argument Long id) {
        divisionService.deleteDivision(id);
        return true;
    }

    private DivisionRequest mapToRequest(DivisionInput input, String tenantId) {
        Long userId = jwtClaimsExtractor.getUserIdOrNull();
        String userIdStr = userId != null ? userId.toString() : null;

        return DivisionRequest.builder()
                .tenantId(tenantId)
                .name(input.getName())
                .code(input.getCode())
                .description(input.getDescription())
                .isActive(input.getIsActive())
                .createdBy(input.getCreatedBy() != null ? input.getCreatedBy() : userIdStr)
                .updatedBy(input.getUpdatedBy() != null ? input.getUpdatedBy() : userIdStr)
                .build();
    }
}
