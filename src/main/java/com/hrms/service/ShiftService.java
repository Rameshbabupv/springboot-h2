package com.hrms.service;

import com.hrms.entity.Shift;
import com.hrms.enums.ShiftType;
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
     * Get shifts with optional filtering.
     * @param tenantId Tenant identifier (required)
     * @param companyId Filter by company (optional)
     * @param isActive Filter by active status (optional)
     * @param shiftType Filter by shift type (optional)
     * @param searchQuery Search by code or name (optional)
     */
    List<Shift> getShifts(String tenantId, Long companyId, Boolean isActive,
                          ShiftType shiftType, String searchQuery);

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
