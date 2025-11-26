package com.hrms.service.impl;

import com.hrms.dto.request.StateRequest;
import com.hrms.entity.State;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.StateRepository;
import com.hrms.service.CountryService;
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
    private final CountryService countryService;

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
    public List<State> getActiveStatesByTenant(String tenantId) {
        log.debug("Fetching active states for tenant: {}", tenantId);
        return stateRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Override
    public List<State> getActiveStates() {
        log.debug("Fetching active states");
        return stateRepository.findByIsActiveTrue();
    }

    @Override
    public List<State> getStatesByCountry(Long countryId) {
        log.debug("Fetching states for country: {}", countryId);
        return stateRepository.findByCountryId(countryId);
    }

    @Override
    public List<State> getStatesByTenantAndCountry(String tenantId, Long countryId) {
        log.debug("Fetching states for tenant: {} and country: {}", tenantId, countryId);
        return stateRepository.findByTenantIdAndCountryId(tenantId, countryId);
    }

    @Override
    public List<State> searchStates(String tenantId, String searchTerm) {
        log.debug("Searching states for tenant: {} with term: {}", tenantId, searchTerm);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getActiveStatesByTenant(tenantId);
        }
        return stateRepository.searchStates(tenantId, searchTerm);
    }

    @Override
    @Transactional
    public State createState(StateRequest request) {
        log.debug("Creating new state: {}", request.getName());

        // Validate that country exists
        if (!countryService.existsById(request.getCountryId())) {
            throw new ResourceNotFoundException("Country", "id", request.getCountryId());
        }

        // Auto-convert code to uppercase
        String upperCode = request.getCode().toUpperCase();
        request.setCode(upperCode);

        // Case-insensitive duplicate check for code
        Optional<State> existingCode = stateRepository
                .findByTenantIdAndCountryIdAndCodeIgnoreCase(
                        request.getTenantId(),
                        request.getCountryId(),
                        request.getCode());
        if (existingCode.isPresent()) {
            throw new DuplicateResourceException("State", "code", request.getCode());
        }

        // Case-insensitive duplicate check for name
        Optional<State> existingName = stateRepository
                .findByTenantIdAndCountryIdAndNameIgnoreCase(
                        request.getTenantId(),
                        request.getCountryId(),
                        request.getName());
        if (existingName.isPresent()) {
            throw new DuplicateResourceException("State", "name", request.getName());
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

        // Validate that country exists
        if (!countryService.existsById(request.getCountryId())) {
            throw new ResourceNotFoundException("Country", "id", request.getCountryId());
        }

        // Auto-convert code to uppercase
        String upperCode = request.getCode().toUpperCase();
        request.setCode(upperCode);

        // Case-insensitive duplicate check for code
        Optional<State> duplicateCode = stateRepository
                .findByTenantIdAndCountryIdAndCodeIgnoreCase(
                        request.getTenantId(),
                        request.getCountryId(),
                        request.getCode());
        if (duplicateCode.isPresent() && !duplicateCode.get().getId().equals(id)) {
            throw new DuplicateResourceException("State", "code", request.getCode());
        }

        // Case-insensitive duplicate check for name
        Optional<State> duplicateName = stateRepository
                .findByTenantIdAndCountryIdAndNameIgnoreCase(
                        request.getTenantId(),
                        request.getCountryId(),
                        request.getName());
        if (duplicateName.isPresent() && !duplicateName.get().getId().equals(id)) {
            throw new DuplicateResourceException("State", "name", request.getName());
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
        state.setCountryId(request.getCountryId());
        state.setName(request.getName());
        state.setCode(request.getCode());
        state.setStateCode(request.getStateCode());
        state.setIsUnionTerritory(request.getIsUnionTerritory() != null ? request.getIsUnionTerritory() : false);
        state.setDescription(request.getDescription());
        state.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        state.setCreatedBy(request.getCreatedBy());
        return state;
    }

    private void updateEntityFromRequest(State state, StateRequest request) {
        state.setTenantId(request.getTenantId());
        state.setCountryId(request.getCountryId());
        state.setName(request.getName());
        state.setCode(request.getCode());
        state.setStateCode(request.getStateCode());
        if (request.getIsUnionTerritory() != null) {
            state.setIsUnionTerritory(request.getIsUnionTerritory());
        }
        state.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            state.setIsActive(request.getIsActive());
        }
        state.setUpdatedBy(request.getUpdatedBy());
    }
}
