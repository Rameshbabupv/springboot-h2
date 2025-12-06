package com.hrms.service;

import com.hrms.entity.Shift;
import com.hrms.graphql.input.ShiftInput;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Shift operations.
 */
public interface ShiftService {

    /**
     * Create a new shift with breaks in a transaction.
     */
    Shift createShift(String tenantId, Long companyId, ShiftInput input);

    /**
     * Update an existing shift (replaces breaks in transaction).
     */
    Shift updateShift(String tenantId, Long id, ShiftInput input);

    /**
     * Delete a shift (checks if used in policies first).
     */
    boolean deleteShift(String tenantId, Long id);

    /**
     * Get shifts for a company.
     */
    List<Shift> getShifts(String tenantId, Long companyId, Boolean isActive);

    /**
     * Get a single shift by ID.
     */
    Optional<Shift> getShift(String tenantId, Long id);

    /**
     * Get shift with breaks eagerly loaded.
     */
    Optional<Shift> getShiftWithBreaks(Long id);

    /**
     * Find shift by code.
     */
    Optional<Shift> findByCode(String tenantId, String code);

    /**
     * Check if shift is used in any policy.
     */
    boolean isShiftUsedInPolicy(Long shiftId);
}
