package com.hrms.service;

import com.hrms.entity.Holiday;
import com.hrms.graphql.input.HolidayInput;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Holiday operations.
 */
public interface HolidayService {

    /**
     * Create a new holiday with company/location mappings.
     */
    Holiday createHoliday(String tenantId, HolidayInput input);

    /**
     * Update an existing holiday.
     */
    Holiday updateHoliday(String tenantId, Long id, HolidayInput input);

    /**
     * Delete a holiday.
     */
    boolean deleteHoliday(String tenantId, Long id);

    /**
     * Bulk create holidays (for templates/imports).
     */
    List<Holiday> bulkCreateHolidays(String tenantId, List<HolidayInput> inputs);

    /**
     * Get holidays with optional filters.
     */
    List<Holiday> getHolidays(String tenantId, Long companyId, Integer year, Long locationId);

    /**
     * Get a single holiday by ID.
     */
    Optional<Holiday> getHoliday(String tenantId, Long id);

    /**
     * Get holiday with mappings eagerly loaded.
     */
    Holiday getHolidayWithMappings(Long id);

    /**
     * Check if a date is a holiday for a company.
     */
    boolean isHoliday(String tenantId, Long companyId, LocalDate date);

    /**
     * Check if a date is a holiday for a location.
     */
    boolean isHolidayForLocation(String tenantId, Long locationId, LocalDate date);

    /**
     * Get holidays in date range for a company.
     */
    List<Holiday> getHolidaysInRange(String tenantId, Long companyId, LocalDate startDate, LocalDate endDate);
}
