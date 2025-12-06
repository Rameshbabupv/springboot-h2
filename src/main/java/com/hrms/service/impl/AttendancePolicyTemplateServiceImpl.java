package com.hrms.service.impl;

import com.hrms.entity.*;
import com.hrms.graphql.input.AttendancePolicyTemplateInput;
import com.hrms.graphql.input.PolicyIncentiveInput;
import com.hrms.graphql.input.WeekoffRuleInput;
import com.hrms.repository.*;
import com.hrms.service.AttendancePolicyTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of AttendancePolicyTemplateService.
 */
@Service
@Transactional
public class AttendancePolicyTemplateServiceImpl implements AttendancePolicyTemplateService {

    private final AttendancePolicyTemplateRepository templateRepository;
    private final PolicyWeekoffRuleRepository weekoffRuleRepository;
    private final PolicyShiftRepository policyShiftRepository;
    private final PolicyIncentiveRepository incentiveRepository;
    private final CompanyRepository companyRepository;
    private final ShiftRepository shiftRepository;

    public AttendancePolicyTemplateServiceImpl(
            AttendancePolicyTemplateRepository templateRepository,
            PolicyWeekoffRuleRepository weekoffRuleRepository,
            PolicyShiftRepository policyShiftRepository,
            PolicyIncentiveRepository incentiveRepository,
            CompanyRepository companyRepository,
            ShiftRepository shiftRepository) {
        this.templateRepository = templateRepository;
        this.weekoffRuleRepository = weekoffRuleRepository;
        this.policyShiftRepository = policyShiftRepository;
        this.incentiveRepository = incentiveRepository;
        this.companyRepository = companyRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public AttendancePolicyTemplate createTemplate(String tenantId, Long companyId,
                                                   AttendancePolicyTemplateInput input) {
        AttendancePolicyTemplate template = new AttendancePolicyTemplate();
        template.setTenantId(tenantId);

        if (companyId != null) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
            template.setCompany(company);
        }

        mapInputToEntity(input, template);
        template = templateRepository.save(template);

        // Add weekoff rules
        if (input.getWeekoffRules() != null) {
            for (WeekoffRuleInput ruleInput : input.getWeekoffRules()) {
                PolicyWeekoffRule rule = new PolicyWeekoffRule(ruleInput.getDayOfWeek(), ruleInput.getPattern());
                template.addWeekoffRule(rule);
            }
        }

        // Add shift assignments
        if (input.getApplicableShiftIds() != null) {
            for (Long shiftId : input.getApplicableShiftIds()) {
                Shift shift = shiftRepository.findById(shiftId)
                        .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + shiftId));
                PolicyShift policyShift = new PolicyShift(template, shift);
                template.addPolicyShift(policyShift);
            }
        }

        // Add incentives
        if (input.getIncentives() != null) {
            for (PolicyIncentiveInput incentiveInput : input.getIncentives()) {
                PolicyIncentive incentive = new PolicyIncentive(
                        incentiveInput.getPayheadCode(), incentiveInput.getCalcType());
                if (incentiveInput.getOverrideValue() != null) {
                    incentive.setOverrideValue(BigDecimal.valueOf(incentiveInput.getOverrideValue()));
                }
                incentive.setIsActive(incentiveInput.getIsActive() != null ? incentiveInput.getIsActive() : true);
                template.addIncentive(incentive);
            }
        }

        return templateRepository.save(template);
    }

    @Override
    public AttendancePolicyTemplate updateTemplate(String tenantId, Long id,
                                                   AttendancePolicyTemplateInput input) {
        AttendancePolicyTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + id));

        if (!template.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Tenant mismatch");
        }

        mapInputToEntity(input, template);

        // Clear and replace related entities
        template.clearRules();

        if (input.getWeekoffRules() != null) {
            for (WeekoffRuleInput ruleInput : input.getWeekoffRules()) {
                PolicyWeekoffRule rule = new PolicyWeekoffRule(ruleInput.getDayOfWeek(), ruleInput.getPattern());
                template.addWeekoffRule(rule);
            }
        }

        if (input.getApplicableShiftIds() != null) {
            for (Long shiftId : input.getApplicableShiftIds()) {
                Shift shift = shiftRepository.findById(shiftId)
                        .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + shiftId));
                PolicyShift policyShift = new PolicyShift(template, shift);
                template.addPolicyShift(policyShift);
            }
        }

        if (input.getIncentives() != null) {
            for (PolicyIncentiveInput incentiveInput : input.getIncentives()) {
                PolicyIncentive incentive = new PolicyIncentive(
                        incentiveInput.getPayheadCode(), incentiveInput.getCalcType());
                if (incentiveInput.getOverrideValue() != null) {
                    incentive.setOverrideValue(BigDecimal.valueOf(incentiveInput.getOverrideValue()));
                }
                incentive.setIsActive(incentiveInput.getIsActive() != null ? incentiveInput.getIsActive() : true);
                template.addIncentive(incentive);
            }
        }

        return templateRepository.save(template);
    }

    @Override
    public boolean deleteTemplate(String tenantId, Long id) {
        AttendancePolicyTemplate template = templateRepository.findById(id).orElse(null);
        if (template == null || !template.getTenantId().equals(tenantId)) {
            return false;
        }
        templateRepository.delete(template);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendancePolicyTemplate> getTemplates(String tenantId, Long companyId, Boolean isActive) {
        if (companyId != null) {
            return templateRepository.findActiveByTenantAndCompany(tenantId, companyId);
        }
        return templateRepository.findByTenantIdOrderByPriorityDesc(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AttendancePolicyTemplate> getTemplate(String tenantId, Long id) {
        return templateRepository.findById(id)
                .filter(t -> t.getTenantId().equals(tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AttendancePolicyTemplate> getTemplateWithRules(Long id) {
        return templateRepository.findByIdWithRules(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AttendancePolicyTemplate> findApplicableTemplate(String tenantId, Long employeeId, LocalDate date) {
        // TODO: Implement criteria matching based on employee's org hierarchy
        // For now, return default template
        return getDefaultTemplate(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AttendancePolicyTemplate> getDefaultTemplate(String tenantId) {
        return templateRepository.findDefaultByTenant(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendancePolicyTemplate> findEffectiveTemplates(String tenantId, Long companyId, LocalDate date) {
        return templateRepository.findEffectiveByTenantAndCompanyAndDate(tenantId, companyId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AttendancePolicyTemplate> findByCode(String tenantId, String code) {
        return templateRepository.findByTenantAndCode(tenantId, code);
    }

    @SuppressWarnings("unchecked")
    private void mapInputToEntity(AttendancePolicyTemplateInput input, AttendancePolicyTemplate template) {
        template.setTemplateCode(input.getTemplateCode());
        template.setTemplateName(input.getTemplateName());
        template.setDescription(input.getDescription());
        template.setIsDefault(input.getIsDefault() != null ? input.getIsDefault() : false);
        template.setIsActive(input.getIsActive() != null ? input.getIsActive() : true);
        template.setPriority(input.getPriority() != null ? input.getPriority() : 0);
        template.setEffectiveFrom(parseDate(input.getEffectiveFrom()));
        template.setEffectiveTo(input.getEffectiveTo() != null ? parseDate(input.getEffectiveTo()) : null);
        template.setCriteria((Map<String, Object>) input.getCriteria());
        template.setTreatAbsenceAsLeave(input.getTreatAbsenceAsLeave() != null ? input.getTreatAbsenceAsLeave() : false);
        template.setMarkHolidays(input.getMarkHolidays() != null ? input.getMarkHolidays() : true);
        template.setMarkWeekoffs(input.getMarkWeekoffs() != null ? input.getMarkWeekoffs() : true);
        template.setMinHoursForPresent(input.getMinHoursForPresent() != null ?
                BigDecimal.valueOf(input.getMinHoursForPresent()) : new BigDecimal("8.00"));
        template.setMinHoursForHalfDay(input.getMinHoursForHalfDay() != null ?
                BigDecimal.valueOf(input.getMinHoursForHalfDay()) : new BigDecimal("4.00"));
        template.setGraceInMinutes(input.getGraceInMinutes() != null ? input.getGraceInMinutes() : 15);
        template.setGraceOutMinutes(input.getGraceOutMinutes() != null ? input.getGraceOutMinutes() : 15);
        template.setFirstHalfEnd(input.getFirstHalfEnd() != null ? parseTime(input.getFirstHalfEnd()) : LocalTime.of(13, 0));
        template.setSecondHalfStart(input.getSecondHalfStart() != null ? parseTime(input.getSecondHalfStart()) : LocalTime.of(14, 0));
        template.setIsOtEligible(input.getIsOtEligible() != null ? input.getIsOtEligible() : false);
        template.setHasIncentives(input.getHasIncentives() != null ? input.getHasIncentives() : false);
    }

    private LocalDate parseDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    }

    private LocalTime parseTime(String time) {
        return LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME);
    }
}
