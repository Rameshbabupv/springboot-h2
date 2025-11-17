package com.hrms.graphql.resolver;

import com.hrms.entity.State;
import com.hrms.graphql.input.StateInput;
import com.hrms.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StateResolver {

    private final StateRepository stateRepository;

    @QueryMapping
    public List<State> states() {
        return stateRepository.findAll();
    }

    @QueryMapping
    public State state(@Argument Long id) {
        return stateRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<State> statesByTenant(@Argument String tenantId) {
        return stateRepository.findByTenantId(tenantId);
    }

    @QueryMapping
    public List<State> activeStates() {
        return stateRepository.findByIsActiveTrue();
    }

    @MutationMapping
    public State createState(@Argument StateInput input) {
        State state = mapToEntity(input);
        return stateRepository.save(state);
    }

    @MutationMapping
    public State updateState(@Argument Long id, @Argument StateInput input) {
        State state = stateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("State not found"));

        updateEntityFromInput(state, input);
        return stateRepository.save(state);
    }

    @MutationMapping
    public Boolean deleteState(@Argument Long id) {
        if (stateRepository.existsById(id)) {
            stateRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private State mapToEntity(StateInput input) {
        State state = new State();
        updateEntityFromInput(state, input);
        return state;
    }

    private void updateEntityFromInput(State state, StateInput input) {
        state.setTenantId(input.getTenantId());
        state.setName(input.getName());
        state.setCode(input.getCode());
        state.setDescription(input.getDescription());
        state.setIsActive(input.getIsActive());
    }
}
