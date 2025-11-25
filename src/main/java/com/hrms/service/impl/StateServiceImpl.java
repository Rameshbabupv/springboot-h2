package com.hrms.service.impl;

import com.hrms.dto.request.StateRequest;
import com.hrms.entity.State;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.StateRepository;
import com.hrms.service.StateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for State operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StateServiceImpl implements StateService {

    private final StateRepository stateRepository;

    @Override
    public List<State> getAllStates() {
        log.debug("Fetching all states");
        return stateRepository.findAll();
    }

    @Override
    public State getStateById(Long id) {
        log.debug("Fetching state with id: {}", id);
        return stateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("State", "id", id));
    }

    @Override
    public List<State> getStatesByTenant(String tenantId) {
        log.debug("Fetching states for tenant: {}", tenantId);
        return stateRepository.findByTenantId(tenantId);
    }

    @Override
    public List<State> getActiveStates() {
        log.debug("Fetching active states");
        return stateRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional
    public State createState(StateRequest request) {
        log.debug("Creating new state: {}", request.getName());

        Optional<State> existing = stateRepository
                .findByTenantIdAndCode(request.getTenantId(), request.getCode());
        if (existing.isPresent()) {
            throw new DuplicateResourceException("State", "code", request.getCode());
        }

        State state = mapToEntity(request);
        State saved = stateRepository.save(state);

        log.info("Created state with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public State updateState(Long id, StateRequest request) {
        log.debug("Updating state with id: {}", id);

        State existing = getStateById(id);

        Optional<State> duplicate = stateRepository
                .findByTenantIdAndCode(request.getTenantId(), request.getCode());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new DuplicateResourceException("State", "code", request.getCode());
        }

        updateEntityFromRequest(existing, request);
        State updated = stateRepository.save(existing);

        log.info("Updated state with id: {}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteState(Long id) {
        log.debug("Deleting state with id: {}", id);

        if (!stateRepository.existsById(id)) {
            throw new ResourceNotFoundException("State", "id", id);
        }
        stateRepository.deleteById(id);

        log.info("Deleted state with id: {}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return stateRepository.existsById(id);
    }

    private State mapToEntity(StateRequest request) {
        State state = new State();
        state.setTenantId(request.getTenantId());
        state.setName(request.getName());
        state.setCode(request.getCode());
        state.setDescription(request.getDescription());
        state.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return state;
    }

    private void updateEntityFromRequest(State state, StateRequest request) {
        state.setTenantId(request.getTenantId());
        state.setName(request.getName());
        state.setCode(request.getCode());
        state.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            state.setIsActive(request.getIsActive());
        }
    }
}
