package com.hrms.service.impl;

import com.hrms.entity.*;
import com.hrms.enums.HolidayType;
import com.hrms.graphql.input.HolidayInput;
import com.hrms.repository.*;
import com.hrms.service.HolidayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of HolidayService.
 */
@Service
@Transactional
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;
    private final HolidayCompanyMappingRepository companyMappingRepository;
    private final HolidayLocationMappingRepository locationMappingRepository;
    private final CompanyRepository companyRepository;
    private final CompanyLocationRepository locationRepository;

    public HolidayServiceImpl(HolidayRepository holidayRepository,
                             HolidayCompanyMappingRepository companyMappingRepository,
                             HolidayLocationMappingRepository locationMappingRepository,
                             CompanyRepository companyRepository,
                             CompanyLocationRepository locationRepository) {
        this.holidayRepository = holidayRepository;
        this.companyMappingRepository = companyMappingRepository;
        this.locationMappingRepository = locationMappingRepository;
        this.companyRepository = companyRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public Holiday createHoliday(String tenantId, HolidayInput input) {
        Holiday holiday = new Holiday();
        holiday.setTenantId(tenantId);
        mapInputToEntity(input, holiday);
        holiday = holidayRepository.save(holiday);

        // Add company mappings
        if (input.getCompanyIds() != null && !input.getCompanyIds().isEmpty()) {
            for (Long companyId : input.getCompanyIds()) {
                Company company = companyRepository.findById(companyId)
                        .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
                HolidayCompanyMapping mapping = new HolidayCompanyMapping(holiday, company);
                holiday.addCompanyMapping(mapping);
            }
        }

        // Add location mappings
        if (input.getLocationIds() != null && !input.getLocationIds().isEmpty()) {
            for (Long locationId : input.getLocationIds()) {
                CompanyLocation location = locationRepository.findById(locationId)
                        .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId));
                HolidayLocationMapping mapping = new HolidayLocationMapping(holiday, location);
                holiday.addLocationMapping(mapping);
            }
        }

        return holidayRepository.save(holiday);
    }

    @Override
    public Holiday updateHoliday(String tenantId, Long id, HolidayInput input) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found: " + id));

        if (!holiday.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Tenant mismatch");
        }

        mapInputToEntity(input, holiday);

        // Clear and replace mappings
        holiday.clearMappings();

        if (input.getCompanyIds() != null && !input.getCompanyIds().isEmpty()) {
            for (Long companyId : input.getCompanyIds()) {
                Company company = companyRepository.findById(companyId)
                        .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
                HolidayCompanyMapping mapping = new HolidayCompanyMapping(holiday, company);
                holiday.addCompanyMapping(mapping);
            }
        }

        if (input.getLocationIds() != null && !input.getLocationIds().isEmpty()) {
            for (Long locationId : input.getLocationIds()) {
                CompanyLocation location = locationRepository.findById(locationId)
                        .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId));
                HolidayLocationMapping mapping = new HolidayLocationMapping(holiday, location);
                holiday.addLocationMapping(mapping);
            }
        }

        return holidayRepository.save(holiday);
    }

    @Override
    public boolean deleteHoliday(String tenantId, Long id) {
        Holiday holiday = holidayRepository.findById(id).orElse(null);
        if (holiday == null || !holiday.getTenantId().equals(tenantId)) {
            return false;
        }
        holidayRepository.delete(holiday);
        return true;
    }

    @Override
    public List<Holiday> bulkCreateHolidays(String tenantId, List<HolidayInput> inputs) {
        List<Holiday> holidays = new ArrayList<>();
        for (HolidayInput input : inputs) {
            holidays.add(createHoliday(tenantId, input));
        }
        return holidays;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Holiday> getHolidays(String tenantId, Long companyId, Integer year, Long locationId, String searchQuery) {
        // Use the flexible filter query for all cases
        return holidayRepository.findHolidaysWithFilters(tenantId, companyId, year, locationId, searchQuery);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Holiday> getHoliday(String tenantId, Long id) {
        return holidayRepository.findById(id)
                .filter(h -> h.getTenantId().equals(tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public Holiday getHolidayWithMappings(Long id) {
        return holidayRepository.findByIdWithMappings(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isHoliday(String tenantId, Long companyId, LocalDate date) {
        return holidayRepository.isHolidayForCompany(tenantId, companyId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isHolidayForLocation(String tenantId, Long locationId, LocalDate date) {
        return holidayRepository.isHolidayForLocation(tenantId, locationId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Holiday> getHolidaysInRange(String tenantId, Long companyId, LocalDate startDate, LocalDate endDate) {
        return holidayRepository.findByTenantAndCompanyAndDateRange(tenantId, companyId, startDate, endDate);
    }

    private void mapInputToEntity(HolidayInput input, Holiday holiday) {
        holiday.setDate(parseDate(input.getDate()));
        holiday.setName(input.getName());
        holiday.setHolidayType(HolidayType.valueOf(input.getHolidayType()));
        holiday.setCategory(input.getCategory());
        holiday.setIsMandatory(input.getIsMandatory() != null ? input.getIsMandatory() : true);
        holiday.setIsNiActCompliant(input.getIsNiActCompliant() != null ? input.getIsNiActCompliant() : false);
        holiday.setIsFloating(input.getIsFloating() != null ? input.getIsFloating() : false);
        holiday.setReligion(input.getReligion());
        holiday.setDescription(input.getDescription());
    }

    private LocalDate parseDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    }
}
