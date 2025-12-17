package com.hrms.service.impl;

import com.hrms.entity.Company;
import com.hrms.entity.PayheadMaster;
import com.hrms.enums.PayheadType;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.graphql.input.PayheadInput;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.EmployeeSalaryStructureRepository;
import com.hrms.repository.PayheadMasterRepository;
import com.hrms.service.PayheadMasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Payhead Master operations
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PayheadMasterServiceImpl implements PayheadMasterService {

    private final PayheadMasterRepository payheadRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeSalaryStructureRepository salaryStructureRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PayheadMaster> getPayheads(String tenantId, Long companyId, PayheadType type, Boolean isActive) {
        log.debug("Fetching payheads - tenant: {}, company: {}, type: {}, active: {}",
                  tenantId, companyId, type, isActive);

        if (type != null) {
            return payheadRepository.findByTenantIdAndCompanyIdAndType(tenantId, companyId, type);
        } else if (Boolean.TRUE.equals(isActive)) {
            return payheadRepository.findActiveByTenantIdAndCompanyId(tenantId, companyId);
        } else {
            return payheadRepository.findByTenantIdAndCompanyId(tenantId, companyId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PayheadMaster getPayheadById(String tenantId, Long id) {
        log.debug("Fetching payhead by ID: {} for tenant: {}", id, tenantId);

        return payheadRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Payhead not found with ID: " + id));
    }

    @Override
    public PayheadMaster createPayhead(String tenantId, Long companyId, PayheadInput input) {
        log.info("Creating payhead: {} for tenant: {}, company: {}",
                 input.getPayheadCode(), tenantId, companyId);

        // Validate payhead code uniqueness
        if (payheadRepository.existsByTenantIdAndCompanyIdAndCode(tenantId, companyId, input.getPayheadCode())) {
            throw new DuplicateResourceException(
                "Payhead with code '" + input.getPayheadCode() + "' already exists");
        }

        // Get company if specified
        Company company = null;
        if (companyId != null) {
            company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));
        }

        // Build entity
        PayheadMaster payhead = PayheadMaster.builder()
            .tenantId(tenantId)
            .company(company)
            .payheadName(input.getPayheadName())
            .payheadCode(input.getPayheadCode())
            .payheadType(input.getPayheadType())
            .payheadCategory(input.getPayheadCategory())
            .calculationType(input.getCalculationType())
            .calculationBase(input.getCalculationBase())
            .defaultValue(input.getDefaultValue())
            .formula(input.getFormula())
            .isTaxable(input.getIsTaxable() != null ? input.getIsTaxable() : true)
            .affectsPf(input.getAffectsPf() != null ? input.getAffectsPf() : false)
            .affectsEsi(input.getAffectsEsi() != null ? input.getAffectsEsi() : false)
            .affectsGratuity(input.getAffectsGratuity() != null ? input.getAffectsGratuity() : false)
            .affectsLwf(input.getAffectsLwf() != null ? input.getAffectsLwf() : false)
            .affectsPt(input.getAffectsPt() != null ? input.getAffectsPt() : false)
            .roundingRule(input.getRoundingRule() != null ? input.getRoundingRule() : "NONE")
            .minValue(input.getMinValue())
            .maxValue(input.getMaxValue())
            .applicableCondition(input.getApplicableCondition())
            .description(input.getDescription())
            .displayOrder(input.getDisplayOrder() != null ? input.getDisplayOrder() : 0)
            .showInPayslip(input.getShowInPayslip() != null ? input.getShowInPayslip() : true)
            .isMandatory(input.getIsMandatory() != null ? input.getIsMandatory() : false)
            .isActive(input.getIsActive() != null ? input.getIsActive() : true)
            .build();

        payhead = payheadRepository.save(payhead);
        log.info("Payhead created successfully with ID: {}", payhead.getId());

        return payhead;
    }

    @Override
    public PayheadMaster updatePayhead(String tenantId, Long id, PayheadInput input) {
        log.info("Updating payhead ID: {} for tenant: {}", id, tenantId);

        PayheadMaster payhead = payheadRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Payhead not found with ID: " + id));

        // Update fields
        payhead.setPayheadName(input.getPayheadName());
        payhead.setPayheadCategory(input.getPayheadCategory());
        payhead.setCalculationType(input.getCalculationType());
        payhead.setCalculationBase(input.getCalculationBase());
        payhead.setDefaultValue(input.getDefaultValue());
        payhead.setFormula(input.getFormula());

        if (input.getIsTaxable() != null) payhead.setIsTaxable(input.getIsTaxable());
        if (input.getAffectsPf() != null) payhead.setAffectsPf(input.getAffectsPf());
        if (input.getAffectsEsi() != null) payhead.setAffectsEsi(input.getAffectsEsi());
        if (input.getAffectsGratuity() != null) payhead.setAffectsGratuity(input.getAffectsGratuity());
        if (input.getAffectsLwf() != null) payhead.setAffectsLwf(input.getAffectsLwf());
        if (input.getAffectsPt() != null) payhead.setAffectsPt(input.getAffectsPt());

        if (input.getRoundingRule() != null) payhead.setRoundingRule(input.getRoundingRule());
        if (input.getMinValue() != null) payhead.setMinValue(input.getMinValue());
        if (input.getMaxValue() != null) payhead.setMaxValue(input.getMaxValue());
        if (input.getApplicableCondition() != null) payhead.setApplicableCondition(input.getApplicableCondition());
        if (input.getDescription() != null) payhead.setDescription(input.getDescription());

        if (input.getDisplayOrder() != null) payhead.setDisplayOrder(input.getDisplayOrder());
        if (input.getShowInPayslip() != null) payhead.setShowInPayslip(input.getShowInPayslip());
        if (input.getIsMandatory() != null) payhead.setIsMandatory(input.getIsMandatory());
        if (input.getIsActive() != null) payhead.setIsActive(input.getIsActive());

        payhead = payheadRepository.save(payhead);
        log.info("Payhead updated successfully");

        return payhead;
    }

    @Override
    public boolean deletePayhead(String tenantId, Long id) {
        log.info("Deleting payhead ID: {} for tenant: {}", id, tenantId);

        PayheadMaster payhead = payheadRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Payhead not found with ID: " + id));

        // Check if payhead is used in any active salary structure
        boolean isUsed = salaryStructureRepository.existsByPayheadIdAndIsActive(id);

        if (isUsed) {
            // Soft delete - just mark as inactive
            log.warn("Payhead {} is in use, performing soft delete", id);
            payhead.setIsActive(false);
            payheadRepository.save(payhead);
        } else {
            // Hard delete
            log.info("Payhead {} is not in use, performing hard delete", id);
            payheadRepository.delete(payhead);
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayheadMaster> getMandatoryPayheads(String tenantId, Long companyId) {
        return payheadRepository.findMandatoryPayheads(tenantId, companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean payheadCodeExists(String tenantId, Long companyId, String code) {
        return payheadRepository.existsByTenantIdAndCompanyIdAndCode(tenantId, companyId, code);
    }
}
