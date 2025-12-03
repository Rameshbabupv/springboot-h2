package com.hrms.service;

import com.hrms.dto.request.StateRequest;
import com.hrms.entity.State;

import java.util.List;

/**
 * Service interface for State operations.
 */
public interface StateService {

    List<State> getAllStates();

    State getStateById(Long id);

    List<State> getStatesByTenant(String tenantId);

    List<State> getActiveStates();

    State createState(StateRequest request);

    State updateState(Long id, StateRequest request);

    void deleteState(Long id);

    boolean existsById(Long id);
}
