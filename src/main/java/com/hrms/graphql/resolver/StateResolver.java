package com.hrms.graphql.resolver;

import com.hrms.dto.request.StateRequest;
import com.hrms.entity.State;
import com.hrms.graphql.input.StateInput;
import com.hrms.service.StateService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StateResolver {

    private final StateService stateService;

    @QueryMapping
    public List<State> states() {
        return stateService.getAllStates();
    }

    @QueryMapping
    public State state(@Argument Long id) {
        return stateService.getStateById(id);
    }

    @QueryMapping
    public List<State> statesByTenant(@Argument String tenantId) {
        return stateService.getStatesByTenant(tenantId);
    }

    @QueryMapping
    public List<State> activeStatesByTenant(@Argument String tenantId) {
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
    public List<State> statesByTenantAndCountry(@Argument String tenantId, @Argument Long countryId) {
        return stateService.getStatesByTenantAndCountry(tenantId, countryId);
    }

    @QueryMapping
    public List<State> searchStates(@Argument String tenantId, @Argument String searchTerm) {
        return stateService.searchStates(tenantId, searchTerm);
    }

    @MutationMapping
    public State createState(@Argument StateInput input) {
        StateRequest request = mapToRequest(input);
        return stateService.createState(request);
    }

    @MutationMapping
    public State updateState(@Argument Long id, @Argument StateInput input) {
        StateRequest request = mapToRequest(input);
        return stateService.updateState(id, request);
    }

    @MutationMapping
    public Boolean deleteState(@Argument Long id) {
        stateService.deleteState(id);
        return true;
    }

    private StateRequest mapToRequest(StateInput input) {
        return StateRequest.builder()
                .tenantId(input.getTenantId())
                .countryId(input.getCountryId())
                .name(input.getName())
                .code(input.getCode())
                .stateCode(input.getStateCode())
                .isUnionTerritory(input.getIsUnionTerritory())
                .description(input.getDescription())
                .isActive(input.getIsActive())
                .createdBy(input.getCreatedBy())
                .updatedBy(input.getUpdatedBy())
                .build();
    }
}
