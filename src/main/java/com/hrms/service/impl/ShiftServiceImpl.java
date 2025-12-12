package com.hrms.service.impl;

import com.hrms.entity.Company;
import com.hrms.entity.Shift;
import com.hrms.entity.ShiftBreak;
import com.hrms.enums.ShiftType;
import com.hrms.graphql.input.ShiftBreakInput;
import com.hrms.graphql.input.ShiftInput;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.PolicyShiftRepository;
import com.hrms.repository.ShiftBreakRepository;
import com.hrms.repository.ShiftRepository;
import com.hrms.service.ShiftService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ShiftService.
 */
@Service
@Transactional
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftBreakRepository shiftBreakRepository;
    private final CompanyRepository companyRepository;
    private final PolicyShiftRepository policyShiftRepository;

    public ShiftServiceImpl(ShiftRepository shiftRepository,
                           ShiftBreakRepository shiftBreakRepository,
                           CompanyRepository companyRepository,
                           PolicyShiftRepository policyShiftRepository) {
        this.shiftRepository = shiftRepository;
        this.shiftBreakRepository = shiftBreakRepository;
        this.companyRepository = companyRepository;
        this.policyShiftRepository = policyShiftRepository;
    }

    @Override
    public Shift createShift(String tenantId, Long companyId, ShiftInput input) {
        Shift shift = new Shift();
        shift.setTenantId(tenantId);

        if (companyId != null) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
            shift.setCompany(company);
        }

        mapInputToEntity(input, shift);
        shift = shiftRepository.save(shift);

        // Add breaks
        if (input.getBreaks() != null) {
            for (ShiftBreakInput breakInput : input.getBreaks()) {
                ShiftBreak shiftBreak = new ShiftBreak();
                mapBreakInputToEntity(breakInput, shiftBreak);
                shift.addBreak(shiftBreak);
            }
        }

        return shiftRepository.save(shift);
    }

    @Override
    public Shift updateShift(String tenantId, Long id, ShiftInput input) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + id));

        if (!shift.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Tenant mismatch");
        }

        mapInputToEntity(input, shift);

        // Replace breaks using the helper method from Shift entity
        List<ShiftBreak> newBreaks = new ArrayList<>();
        if (input.getBreaks() != null) {
            for (ShiftBreakInput breakInput : input.getBreaks()) {
                ShiftBreak shiftBreak = new ShiftBreak();
                mapBreakInputToEntity(breakInput, shiftBreak);
                newBreaks.add(shiftBreak);
            }
        }
        shift.replaceBreaks(newBreaks);

        return shiftRepository.save(shift);
    }

    @Override
    public boolean deleteShift(String tenantId, Long id) {
        Shift shift = shiftRepository.findById(id).orElse(null);
        if (shift == null || !shift.getTenantId().equals(tenantId)) {
            return false;
        }

        if (isShiftUsedInPolicy(id)) {
            throw new IllegalStateException("Cannot delete shift: used in attendance policies");
        }

        shiftRepository.delete(shift);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shift> getShifts(String tenantId, Long companyId, Boolean isActive,
                                  ShiftType shiftType, String searchQuery) {
        // Use the flexible filter query for all cases
        return shiftRepository.findShiftsWithFilters(tenantId, companyId, isActive, shiftType, searchQuery);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Shift> getShift(String tenantId, Long id) {
        return shiftRepository.findById(id)
                .filter(s -> s.getTenantId().equals(tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Shift> getShiftWithBreaks(Long id) {
        return shiftRepository.findByIdWithBreaks(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Shift> findByCode(String tenantId, String code) {
        return shiftRepository.findByTenantAndCode(tenantId, code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isShiftUsedInPolicy(Long shiftId) {
        return policyShiftRepository.countByPolicyTemplateId(shiftId) > 0;
    }

    private void mapInputToEntity(ShiftInput input, Shift shift) {
        shift.setCode(input.getCode());
        shift.setName(input.getName());
        shift.setDescription(input.getDescription());
        shift.setShiftType(input.getShiftType());
        shift.setTimingMode(input.getTimingMode());
        shift.setStartTime(parseTime(input.getStartTime()));
        shift.setEndTime(parseTime(input.getEndTime()));
        shift.setWorkingHours(BigDecimal.valueOf(input.getWorkingHours()));
        shift.setBreakDurationMinutes(input.getBreakDurationMinutes());
        shift.setIsNightShift(input.getIsNightShift() != null ? input.getIsNightShift() : false);
        shift.setIsOtEligible(input.getIsOtEligible() != null ? input.getIsOtEligible() : false);
        shift.setFirstHalfEnd(input.getFirstHalfEnd() != null ? parseTime(input.getFirstHalfEnd()) : null);
        shift.setSecondHalfStart(input.getSecondHalfStart() != null ? parseTime(input.getSecondHalfStart()) : null);
        shift.setIsActive(input.getIsActive() != null ? input.getIsActive() : true);
        shift.setDisplayOrder(input.getDisplayOrder() != null ? input.getDisplayOrder() : 0);
    }

    private void mapBreakInputToEntity(ShiftBreakInput input, ShiftBreak shiftBreak) {
        shiftBreak.setBreakType(input.getBreakType());
        shiftBreak.setName(input.getName());
        shiftBreak.setStartTime(parseTime(input.getStartTime()));
        shiftBreak.setEndTime(parseTime(input.getEndTime()));
        shiftBreak.setDurationMinutes(input.getDurationMinutes());
        shiftBreak.setIsPaid(input.getIsPaid() != null ? input.getIsPaid() : false);
    }

    private LocalTime parseTime(String time) {
        if (time == null) return null;
        return LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME);
    }
}
