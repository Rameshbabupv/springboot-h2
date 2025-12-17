package com.hrms.service.impl;

import com.hrms.dto.request.*;
import com.hrms.entity.*;
import com.hrms.exception.BadRequestException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.*;
import com.hrms.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for Company operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyStatutoryRepository statutoryRepository;
    private final CompanyGeneralSettingsRepository generalSettingsRepository;
    private final CompanyLocationRepository locationRepository;
    private final CompanyBankAccountRepository bankAccountRepository;

    // Dependency validation repositories (for deleteCompany constraint checks)
    // Only repositories for entities with company relationship (NOT tenant-wide masters)
    private final EmployeeRepository employeeRepository;
    private final EmployeeSalaryStructureRepository employeeSalaryStructureRepository;
    private final AttendanceRegularizationRepository attendanceRegularizationRepository;
    private final ShiftRepository shiftRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final AttendancePolicyTemplateRepository attendancePolicyTemplateRepository;

    // ==================== Company CRUD ====================

    @Override
    public List<Company> getAllCompanies() {
        log.debug("Fetching all companies");
        List<Company> result = companyRepository.findAll();
        log.info("Service Response: getAllCompanies - returned {} companies", result.size());
        return result;
    }

    @Override
    public Company getCompanyById(Long id) {
        log.debug("Fetching company with id: {}", id);
        Company result = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        log.info("Service Response: getCompanyById - returned company: {} ({})", result.getName(), result.getCode());
        return result;
    }

    @Override
    public Company getCompanyByCode(String code) {
        log.debug("Fetching company with code: {}", code);
        return companyRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "code", code));
    }

    @Override
    public List<Company> getCompaniesByTenant(String tenantId) {
        log.debug("Fetching companies for tenant: {}", tenantId);
        List<Company> result = companyRepository.findByTenantId(tenantId);
        log.info("Service Response: getCompaniesByTenant - returned {} companies for tenant: {}", result.size(), tenantId);
        return result;
    }

    @Override
    public List<Company> getActiveCompanies() {
        log.debug("Fetching active companies");
        List<Company> result = companyRepository.findByIsActiveTrue();
        log.info("Service Response: getActiveCompanies - returned {} active companies", result.size());
        return result;
    }

    @Override
    @Transactional
    public Company createCompany(CompanyRequest request) {
        log.debug("Creating new company: {}", request.getName());

        Company company = mapToEntity(request);

        // Create default statutory and general settings
        CompanyStatutory statutory = new CompanyStatutory();
        statutory.setCompany(company);
        company.setStatutory(statutory);

        CompanyGeneralSettings generalSettings = new CompanyGeneralSettings();
        generalSettings.setCompany(company);
        company.setGeneralSettings(generalSettings);

        Company saved = companyRepository.save(company);

        log.info("Created company with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Company updateCompany(Long id, CompanyRequest request) {
        log.debug("Updating company with id: {}", id);

        Company existing = getCompanyById(id);
        updateEntityFromRequest(existing, request);
        Company updated = companyRepository.save(existing);

        log.info("Updated company with id: {}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteCompany(Long id) {
        log.debug("Deleting company with id: {}", id);

        if (!companyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Company", "id", id);
        }

        // Validate critical dependencies before allowing deletion
        validateCompanyDeletion(id);

        companyRepository.deleteById(id);

        log.info("Deleted company with id: {}", id);
    }

    /**
     * Validates that a company can be safely deleted.
     * Throws BadRequestException if critical dependent records exist.
     *
     * Hybrid Approach (ADR-003):
     * - Prevent deletion: Company-scoped entities with foreign key relationship
     * - Allow deletion of: Tenant-wide masters (Department, Designation, etc. - no company_id)
     * - Cascade delete: Operational logs (handled by entity cascade config)
     */
    private void validateCompanyDeletion(Long companyId) {
        log.debug("Validating company deletion constraints for company id: {}", companyId);

        // CRITICAL: Prevent deletion if company has active employees
        long employeeCount = employeeRepository.countByCompanyId(companyId);
        if (employeeCount > 0) {
            throw new BadRequestException(
                "Cannot delete company: " + employeeCount + " employee(s) assigned to this company. " +
                "Please reassign or delete employees first."
            );
        }

        // CRITICAL: Prevent deletion if company has salary structures
        long salaryStructureCount = employeeSalaryStructureRepository.countByCompanyId(companyId);
        if (salaryStructureCount > 0) {
            throw new BadRequestException(
                "Cannot delete company: " + salaryStructureCount + " salary structure(s) configured. " +
                "Please remove salary structures first."
            );
        }

        // CRITICAL: Prevent deletion if company has pending attendance records
        long attendanceRegularizationCount = attendanceRegularizationRepository.countByCompanyId(companyId);
        if (attendanceRegularizationCount > 0) {
            throw new BadRequestException(
                "Cannot delete company: " + attendanceRegularizationCount + " pending attendance regularization(s). " +
                "Please resolve or remove regularizations first."
            );
        }

        // ATTENDANCE: Prevent deletion if company has shift/leave configurations
        long shiftCount = shiftRepository.countByCompanyId(companyId);
        if (shiftCount > 0) {
            throw new BadRequestException(
                "Cannot delete company: " + shiftCount + " shift(s) configured. " +
                "Please delete shifts first."
            );
        }

        long leaveTypeCount = leaveTypeRepository.countByCompanyId(companyId);
        if (leaveTypeCount > 0) {
            throw new BadRequestException(
                "Cannot delete company: " + leaveTypeCount + " leave type(s) configured. " +
                "Please delete leave types first."
            );
        }

        long leavePolicyCount = leavePolicyRepository.countByCompanyId(companyId);
        if (leavePolicyCount > 0) {
            throw new BadRequestException(
                "Cannot delete company: " + leavePolicyCount + " leave policy(ies) configured. " +
                "Please delete leave policies first."
            );
        }

        long attendancePolicyCount = attendancePolicyTemplateRepository.countByCompanyId(companyId);
        if (attendancePolicyCount > 0) {
            throw new BadRequestException(
                "Cannot delete company: " + attendancePolicyCount + " attendance policy template(s) configured. " +
                "Please delete attendance policies first."
            );
        }

        log.debug("Company deletion validation passed for company id: {}", companyId);
    }

    @Override
    public boolean existsById(Long id) {
        return companyRepository.existsById(id);
    }

    // ==================== Statutory ====================

    @Override
    public CompanyStatutory getStatutoryByCompanyId(Long companyId) {
        log.debug("Fetching statutory for company: {}", companyId);
        return statutoryRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyStatutory", "companyId", companyId));
    }

    @Override
    @Transactional
    public CompanyStatutory updateStatutory(Long companyId, CompanyStatutoryRequest request) {
        log.debug("Updating statutory for company: {}", companyId);

        Company company = getCompanyById(companyId);
        CompanyStatutory statutory = statutoryRepository.findByCompanyId(companyId)
                .orElseGet(() -> {
                    CompanyStatutory newStatutory = new CompanyStatutory();
                    newStatutory.setCompany(company);
                    return newStatutory;
                });

        updateStatutoryFromRequest(statutory, request);
        CompanyStatutory saved = statutoryRepository.save(statutory);

        log.info("Updated statutory for company: {}", companyId);
        return saved;
    }

    // ==================== General Settings ====================

    @Override
    public CompanyGeneralSettings getGeneralSettingsByCompanyId(Long companyId) {
        log.debug("Fetching general settings for company: {}", companyId);
        return generalSettingsRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyGeneralSettings", "companyId", companyId));
    }

    @Override
    @Transactional
    public CompanyGeneralSettings updateGeneralSettings(Long companyId, CompanyGeneralSettingsRequest request) {
        log.debug("Updating general settings for company: {}", companyId);

        Company company = getCompanyById(companyId);
        CompanyGeneralSettings settings = generalSettingsRepository.findByCompanyId(companyId)
                .orElseGet(() -> {
                    CompanyGeneralSettings newSettings = new CompanyGeneralSettings();
                    newSettings.setCompany(company);
                    return newSettings;
                });

        updateGeneralSettingsFromRequest(settings, request);
        CompanyGeneralSettings saved = generalSettingsRepository.save(settings);

        log.info("Updated general settings for company: {}", companyId);
        return saved;
    }

    // ==================== Locations ====================

    @Override
    public List<CompanyLocation> getLocationsByCompanyId(Long companyId) {
        log.debug("Fetching locations for company: {}", companyId);
        return locationRepository.findByCompanyId(companyId);
    }

    @Override
    public CompanyLocation getLocationById(Long id) {
        log.debug("Fetching location with id: {}", id);
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyLocation", "id", id));
    }

    @Override
    @Transactional
    public CompanyLocation createLocation(Long companyId, CompanyLocationRequest request) {
        log.debug("Creating location for company: {}", companyId);

        Company company = getCompanyById(companyId);
        CompanyLocation location = mapToLocationEntity(request);
        location.setCompany(company);

        CompanyLocation saved = locationRepository.save(location);

        log.info("Created location with id: {} for company: {}", saved.getId(), companyId);
        return saved;
    }

    @Override
    @Transactional
    public CompanyLocation updateLocation(Long id, CompanyLocationRequest request) {
        log.debug("Updating location with id: {}", id);

        CompanyLocation existing = getLocationById(id);
        updateLocationFromRequest(existing, request);
        CompanyLocation updated = locationRepository.save(existing);

        log.info("Updated location with id: {}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteLocation(Long id) {
        log.debug("Deleting location with id: {}", id);

        if (!locationRepository.existsById(id)) {
            throw new ResourceNotFoundException("CompanyLocation", "id", id);
        }
        locationRepository.deleteById(id);

        log.info("Deleted location with id: {}", id);
    }

    // ==================== Bank Accounts ====================

    @Override
    public List<CompanyBankAccount> getBankAccountsByCompanyId(Long companyId) {
        log.debug("Fetching bank accounts for company: {}", companyId);
        return bankAccountRepository.findByCompanyId(companyId);
    }

    @Override
    public CompanyBankAccount getBankAccountById(Long id) {
        log.debug("Fetching bank account with id: {}", id);
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyBankAccount", "id", id));
    }

    @Override
    @Transactional
    public CompanyBankAccount createBankAccount(Long companyId, CompanyBankAccountRequest request) {
        log.debug("Creating bank account for company: {}", companyId);

        Company company = getCompanyById(companyId);
        CompanyBankAccount bankAccount = mapToBankAccountEntity(request);
        bankAccount.setCompany(company);

        // If this is the first account or marked as primary, set it as primary
        if (request.getIsPrimary() != null && request.getIsPrimary()) {
            bankAccountRepository.resetPrimaryForCompany(companyId);
            bankAccount.setIsPrimary(true);
        }

        CompanyBankAccount saved = bankAccountRepository.save(bankAccount);

        log.info("Created bank account with id: {} for company: {}", saved.getId(), companyId);
        return saved;
    }

    @Override
    @Transactional
    public CompanyBankAccount updateBankAccount(Long id, CompanyBankAccountRequest request) {
        log.debug("Updating bank account with id: {}", id);

        CompanyBankAccount existing = getBankAccountById(id);

        // Handle primary flag
        if (request.getIsPrimary() != null && request.getIsPrimary() && !existing.getIsPrimary()) {
            bankAccountRepository.resetPrimaryForCompany(existing.getCompany().getId());
        }

        updateBankAccountFromRequest(existing, request);
        CompanyBankAccount updated = bankAccountRepository.save(existing);

        log.info("Updated bank account with id: {}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteBankAccount(Long id) {
        log.debug("Deleting bank account with id: {}", id);

        if (!bankAccountRepository.existsById(id)) {
            throw new ResourceNotFoundException("CompanyBankAccount", "id", id);
        }
        bankAccountRepository.deleteById(id);

        log.info("Deleted bank account with id: {}", id);
    }

    @Override
    @Transactional
    public CompanyBankAccount setPrimaryBankAccount(Long id) {
        log.debug("Setting bank account {} as primary", id);

        CompanyBankAccount bankAccount = getBankAccountById(id);
        bankAccountRepository.resetPrimaryForCompany(bankAccount.getCompany().getId());
        bankAccount.setIsPrimary(true);
        CompanyBankAccount saved = bankAccountRepository.save(bankAccount);

        log.info("Set bank account {} as primary for company: {}", id, bankAccount.getCompany().getId());
        return saved;
    }

    // ==================== Mapping Methods ====================

    private Company mapToEntity(CompanyRequest request) {
        Company company = new Company();
        company.setTenantId(request.getTenantId());
        company.setCode(request.getCode());
        company.setName(request.getName());
        company.setShortName(request.getShortName());
        company.setIndustry(request.getIndustry());
        company.setIndustryDescription(request.getIndustryDescription());
        company.setCompanyType(request.getCompanyType());
        company.setLogo(request.getLogo());
        company.setAddressLine1(request.getAddressLine1());
        company.setAddressLine2(request.getAddressLine2());
        company.setCountry(request.getCountry() != null ? request.getCountry() : "India");
        company.setState(request.getState());
        company.setCity(request.getCity());
        company.setPincode(request.getPincode());
        company.setPrimaryPhone(request.getPrimaryPhone());
        company.setAlternatePhone(request.getAlternatePhone());
        company.setEmail(request.getEmail());
        company.setWebsite(request.getWebsite());
        company.setContactName(request.getContactName());
        company.setContactDesignation(request.getContactDesignation());
        company.setContactEmail(request.getContactEmail());
        company.setContactPhone(request.getContactPhone());
        company.setAdminNotes(request.getAdminNotes());
        company.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return company;
    }

    private void updateEntityFromRequest(Company company, CompanyRequest request) {
        company.setTenantId(request.getTenantId());
        company.setCode(request.getCode());
        company.setName(request.getName());
        company.setShortName(request.getShortName());
        company.setIndustry(request.getIndustry());
        company.setIndustryDescription(request.getIndustryDescription());
        company.setCompanyType(request.getCompanyType());
        company.setLogo(request.getLogo());
        company.setAddressLine1(request.getAddressLine1());
        company.setAddressLine2(request.getAddressLine2());
        if (request.getCountry() != null) company.setCountry(request.getCountry());
        company.setState(request.getState());
        company.setCity(request.getCity());
        company.setPincode(request.getPincode());
        company.setPrimaryPhone(request.getPrimaryPhone());
        company.setAlternatePhone(request.getAlternatePhone());
        company.setEmail(request.getEmail());
        company.setWebsite(request.getWebsite());
        company.setContactName(request.getContactName());
        company.setContactDesignation(request.getContactDesignation());
        company.setContactEmail(request.getContactEmail());
        company.setContactPhone(request.getContactPhone());
        company.setAdminNotes(request.getAdminNotes());
        if (request.getIsActive() != null) company.setIsActive(request.getIsActive());
    }

    private void updateStatutoryFromRequest(CompanyStatutory statutory, CompanyStatutoryRequest request) {
        statutory.setPan(request.getPan());
        statutory.setTan(request.getTan());
        statutory.setCin(request.getCin());
        statutory.setLin(request.getLin());
        statutory.setGstin(request.getGstin());

        if (request.getPfEnabled() != null) statutory.setPfEnabled(request.getPfEnabled());
        statutory.setPfAccountNumber(request.getPfAccountNumber());
        if (request.getPfCeiling() != null) statutory.setPfCeiling(request.getPfCeiling());
        if (request.getPfEmployeeRate() != null) statutory.setPfEmployeeRate(request.getPfEmployeeRate());
        if (request.getPfEmployerRate() != null) statutory.setPfEmployerRate(request.getPfEmployerRate());
        if (request.getPfEmployerEpfRate() != null) statutory.setPfEmployerEpfRate(request.getPfEmployerEpfRate());
        if (request.getPfEmployerEpsRate() != null) statutory.setPfEmployerEpsRate(request.getPfEmployerEpsRate());

        if (request.getEsiEnabled() != null) statutory.setEsiEnabled(request.getEsiEnabled());
        statutory.setEsiNumber(request.getEsiNumber());
        if (request.getEsiCeiling() != null) statutory.setEsiCeiling(request.getEsiCeiling());
        if (request.getEsiEmployeeRate() != null) statutory.setEsiEmployeeRate(request.getEsiEmployeeRate());
        if (request.getEsiEmployerRate() != null) statutory.setEsiEmployerRate(request.getEsiEmployerRate());

        if (request.getPtEnabled() != null) statutory.setPtEnabled(request.getPtEnabled());
        statutory.setPtState(request.getPtState());
        statutory.setPtRegistrationNumber(request.getPtRegistrationNumber());
        if (request.getPtRegistrationDate() != null) {
            statutory.setPtRegistrationDate(LocalDate.parse(request.getPtRegistrationDate()));
        }
        if (request.getPtValidUpto() != null) {
            statutory.setPtValidUpto(LocalDate.parse(request.getPtValidUpto()));
        }

        if (request.getRetirementAge() != null) statutory.setRetirementAge(request.getRetirementAge());
        statutory.setTdsType(request.getTdsType());
        if (request.getAllowTdsOverride() != null) statutory.setAllowTdsOverride(request.getAllowTdsOverride());
        if (request.getApplicableActs() != null) {
            statutory.setApplicableActsList(request.getApplicableActs());
        }
    }

    private void updateGeneralSettingsFromRequest(CompanyGeneralSettings settings, CompanyGeneralSettingsRequest request) {
        if (request.getEnableDivisions() != null) settings.setEnableDivisions(request.getEnableDivisions());
        if (request.getEnableDepartment() != null) settings.setEnableDepartment(request.getEnableDepartment());
        if (request.getEnableSection() != null) settings.setEnableSection(request.getEnableSection());
        if (request.getEnableGrade() != null) settings.setEnableGrade(request.getEnableGrade());
        if (request.getCurrency() != null) settings.setCurrency(request.getCurrency());
        if (request.getDateFormat() != null) settings.setDateFormat(request.getDateFormat());
        if (request.getTimeZone() != null) settings.setTimeZone(request.getTimeZone());
        if (request.getFinancialYearStart() != null) settings.setFinancialYearStart(request.getFinancialYearStart());
        if (request.getLanguage() != null) settings.setLanguage(request.getLanguage());
    }

    private CompanyLocation mapToLocationEntity(CompanyLocationRequest request) {
        CompanyLocation location = new CompanyLocation();
        location.setType(request.getType());
        location.setName(request.getName());
        location.setCode(request.getCode());
        location.setAddressLine1(request.getAddressLine1());
        location.setAddressLine2(request.getAddressLine2());
        location.setState(request.getState());
        location.setCity(request.getCity());
        location.setPincode(request.getPincode());
        location.setEsiNumber(request.getEsiNumber());
        location.setPfNumber(request.getPfNumber());
        location.setPtNumber(request.getPtNumber());
        location.setGstin(request.getGstin());
        location.setLicenseNumber(request.getLicenseNumber());
        location.setContactName(request.getContactName());
        location.setContactPhone(request.getContactPhone());
        location.setContactEmail(request.getContactEmail());
        location.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return location;
    }

    private void updateLocationFromRequest(CompanyLocation location, CompanyLocationRequest request) {
        location.setType(request.getType());
        location.setName(request.getName());
        location.setCode(request.getCode());
        location.setAddressLine1(request.getAddressLine1());
        location.setAddressLine2(request.getAddressLine2());
        location.setState(request.getState());
        location.setCity(request.getCity());
        location.setPincode(request.getPincode());
        location.setEsiNumber(request.getEsiNumber());
        location.setPfNumber(request.getPfNumber());
        location.setPtNumber(request.getPtNumber());
        location.setGstin(request.getGstin());
        location.setLicenseNumber(request.getLicenseNumber());
        location.setContactName(request.getContactName());
        location.setContactPhone(request.getContactPhone());
        location.setContactEmail(request.getContactEmail());
        if (request.getIsActive() != null) location.setIsActive(request.getIsActive());
    }

    private CompanyBankAccount mapToBankAccountEntity(CompanyBankAccountRequest request) {
        CompanyBankAccount bankAccount = new CompanyBankAccount();
        bankAccount.setBeneficiaryName(request.getBeneficiaryName());
        bankAccount.setAccountName(request.getAccountName());
        bankAccount.setBankName(request.getBankName());
        bankAccount.setBranchName(request.getBranchName());
        bankAccount.setAccountNumber(request.getAccountNumber());
        bankAccount.setIfscCode(request.getIfscCode());
        bankAccount.setAccountType(request.getAccountType());
        bankAccount.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
        bankAccount.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return bankAccount;
    }

    private void updateBankAccountFromRequest(CompanyBankAccount bankAccount, CompanyBankAccountRequest request) {
        bankAccount.setBeneficiaryName(request.getBeneficiaryName());
        bankAccount.setAccountName(request.getAccountName());
        bankAccount.setBankName(request.getBankName());
        bankAccount.setBranchName(request.getBranchName());
        bankAccount.setAccountNumber(request.getAccountNumber());
        bankAccount.setIfscCode(request.getIfscCode());
        bankAccount.setAccountType(request.getAccountType());
        if (request.getIsPrimary() != null) bankAccount.setIsPrimary(request.getIsPrimary());
        if (request.getIsActive() != null) bankAccount.setIsActive(request.getIsActive());
    }
}
