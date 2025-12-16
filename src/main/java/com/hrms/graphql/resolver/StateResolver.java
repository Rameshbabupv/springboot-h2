package com.hrms.graphql.resolver;

import com.hrms.dto.request.StateRequest;
import com.hrms.entity.State;
import com.hrms.graphql.input.StateInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.StateService;
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
public class StateResolver {

    private final StateService stateService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    @QueryMapping
    public List<State> states() {
        return stateService.getAllStates();
    }

    @QueryMapping
    public State state(@Argument Long id) {
        return stateService.getStateById(id);
    }

    @QueryMapping
    public List<State> statesByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return stateService.getStatesByTenant(tenantId);
    }

    @QueryMapping
    public List<State> activeStatesByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return stateService.getActiveStatesByTenant(tenantId);
    }

    @QueryMapping
    public List<State> activeStates() {
        return stateService.getActiveStates();
    }

    @QueryMapping
    public List<State> statesByCountry(@Argument Long countryId) {
        return stateService.getStatesByCountry(countryId);
    }

    @QueryMapping
    public List<State> statesByTenantAndCountry(@Argument(name = "tenantId") String tenantIdArg, @Argument Long countryId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return stateService.getStatesByTenantAndCountry(tenantId, countryId);
    }

    @QueryMapping
    public List<State> searchStates(@Argument(name = "tenantId") String tenantIdArg, @Argument String searchTerm) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return stateService.searchStates(tenantId, searchTerm);
    }

    @MutationMapping
    public State createState(@Argument StateInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        StateRequest request = mapToRequest(input, tenantId);
        return stateService.createState(request);
    }

    @MutationMapping
    public State updateState(@Argument Long id, @Argument StateInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        StateRequest request = mapToRequest(input, tenantId);
        return stateService.updateState(id, request);
    }

    @MutationMapping
    public Boolean deleteState(@Argument Long id) {
        stateService.deleteState(id);
        return true;
    }

    private StateRequest mapToRequest(StateInput input, String tenantId) {
        Long userId = jwtClaimsExtractor.getUserIdOrNull();
        String userIdStr = userId != null ? userId.toString() : null;

        return StateRequest.builder()
                .tenantId(tenantId)
                .countryId(input.getCountryId())
                .name(input.getName())
                .code(input.getCode())
                .stateCode(input.getStateCode())
                .isUnionTerritory(input.getIsUnionTerritory())
                .description(input.getDescription())
                .isActive(input.getIsActive())
                .createdBy(input.getCreatedBy() != null ? input.getCreatedBy() : userIdStr)
                .updatedBy(input.getUpdatedBy() != null ? input.getUpdatedBy() : userIdStr)
                .build();
    }
}
