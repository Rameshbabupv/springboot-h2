package com.hrms.graphql.resolver;

import com.hrms.dto.request.*;
import com.hrms.entity.*;
import com.hrms.graphql.input.*;
import com.hrms.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CompanyResolver {

    private final CompanyService companyService;

    // ==================== Company Queries ====================

    @QueryMapping
    public List<Company> companies() {
        return companyService.getAllCompanies();
    }

    @QueryMapping
    public Company company(@Argument Long id) {
        return companyService.getCompanyById(id);
    }

    @QueryMapping
    public Company companyByCode(@Argument String code) {
        return companyService.getCompanyByCode(code);
    }

    @QueryMapping
    public List<Company> companiesByTenant(@Argument String tenantId) {
        return companyService.getCompaniesByTenant(tenantId);
    }

    @QueryMapping
    public List<Company> activeCompanies() {
        return companyService.getActiveCompanies();
    }

    // ==================== Company Mutations ====================

    @MutationMapping
    public Company createCompany(@Argument CompanyInput input) {
        System.out.println("=== DEBUG: createCompany called ===");
        System.out.println("DEBUG: input object class: " + input.getClass().getName());
        System.out.println("DEBUG: input object toString: " + input);
        System.out.println("Input tenantId: " + input.getTenantId());
        System.out.println("Input code: " + input.getCode());
        System.out.println("Input name: " + input.getName());
        System.out.println("Input shortName: " + input.getShortName());
        System.out.println("Input industry: " + input.getIndustry());
        System.out.println("Input logoUrl: " + (input.getLogoUrl() != null ? input.getLogoUrl().substring(0, Math.min(50, input.getLogoUrl().length())) : "null"));

        CompanyRequest request = mapToCompanyRequest(input);

        System.out.println("Request shortName: " + request.getShortName());
        System.out.println("Request logo: " + (request.getLogo() != null ? request.getLogo().substring(0, Math.min(50, request.getLogo().length())) : "null"));

        Company result = companyService.createCompany(request);

        System.out.println("Result shortName: " + result.getShortName());
        System.out.println("Result logo: " + (result.getLogo() != null ? result.getLogo().substring(0, Math.min(50, result.getLogo().length())) : "null"));

        return result;
    }

    @MutationMapping
    public Company updateCompany(@Argument Long id, @Argument CompanyInput input) {
        CompanyRequest request = mapToCompanyRequest(input);
        return companyService.updateCompany(id, request);
    }

    @MutationMapping
    public Boolean deleteCompany(@Argument Long id) {
        companyService.deleteCompany(id);
        return true;
    }

    // ==================== Statutory Queries/Mutations ====================

    @QueryMapping
    public CompanyStatutory companyStatutory(@Argument Long companyId) {
        return companyService.getStatutoryByCompanyId(companyId);
    }

    @MutationMapping
    public CompanyStatutory updateCompanyStatutory(@Argument Long companyId, @Argument CompanyStatutoryInput input) {
        CompanyStatutoryRequest request = mapToStatutoryRequest(input);
        return companyService.updateStatutory(companyId, request);
    }

    // ==================== General Settings Queries/Mutations ====================

    @QueryMapping
    public CompanyGeneralSettings companyGeneralSettings(@Argument Long companyId) {
        return companyService.getGeneralSettingsByCompanyId(companyId);
    }

    @MutationMapping
    public CompanyGeneralSettings updateCompanyGeneralSettings(@Argument Long companyId, @Argument CompanyGeneralSettingsInput input) {
        CompanyGeneralSettingsRequest request = mapToGeneralSettingsRequest(input);
        return companyService.updateGeneralSettings(companyId, request);
    }

    // ==================== Location Queries/Mutations ====================

    @QueryMapping
    public List<CompanyLocation> companyLocations(@Argument Long companyId) {
        return companyService.getLocationsByCompanyId(companyId);
    }

    @QueryMapping
    public CompanyLocation companyLocation(@Argument Long id) {
        return companyService.getLocationById(id);
    }

    @MutationMapping
    public CompanyLocation createCompanyLocation(@Argument Long companyId, @Argument CompanyLocationInput input) {
        CompanyLocationRequest request = mapToLocationRequest(input);
        return companyService.createLocation(companyId, request);
    }

    @MutationMapping
    public CompanyLocation updateCompanyLocation(@Argument Long id, @Argument CompanyLocationInput input) {
        CompanyLocationRequest request = mapToLocationRequest(input);
        return companyService.updateLocation(id, request);
    }

    @MutationMapping
    public Boolean deleteCompanyLocation(@Argument Long id) {
        companyService.deleteLocation(id);
        return true;
    }

    // ==================== Bank Account Queries/Mutations ====================

    @QueryMapping
    public List<CompanyBankAccount> companyBankAccounts(@Argument Long companyId) {
        return companyService.getBankAccountsByCompanyId(companyId);
    }

    @QueryMapping
    public CompanyBankAccount companyBankAccount(@Argument Long id) {
        return companyService.getBankAccountById(id);
    }

    @MutationMapping
    public CompanyBankAccount createCompanyBankAccount(@Argument Long companyId, @Argument CompanyBankAccountInput input) {
        CompanyBankAccountRequest request = mapToBankAccountRequest(input);
        return companyService.createBankAccount(companyId, request);
    }

    @MutationMapping
    public CompanyBankAccount updateCompanyBankAccount(@Argument Long id, @Argument CompanyBankAccountInput input) {
        CompanyBankAccountRequest request = mapToBankAccountRequest(input);
        return companyService.updateBankAccount(id, request);
    }

    @MutationMapping
    public Boolean deleteCompanyBankAccount(@Argument Long id) {
        companyService.deleteBankAccount(id);
        return true;
    }

    @MutationMapping
    public CompanyBankAccount setCompanyPrimaryBankAccount(@Argument Long id) {
        return companyService.setPrimaryBankAccount(id);
    }

    // ==================== Schema Mappings for nested fields ====================

    @SchemaMapping(typeName = "Company", field = "logoUrl")
    public String getLogoUrl(Company company) {
        return company.getLogo();
    }

    @SchemaMapping(typeName = "Company", field = "statutory")
    public CompanyStatutory getStatutory(Company company) {
        return company.getStatutory();
    }

    @SchemaMapping(typeName = "Company", field = "generalSettings")
    public CompanyGeneralSettings getGeneralSettings(Company company) {
        return company.getGeneralSettings();
    }

    @SchemaMapping(typeName = "Company", field = "locations")
    public List<CompanyLocation> getLocations(Company company) {
        return company.getLocations();
    }

    @SchemaMapping(typeName = "Company", field = "bankAccounts")
    public List<CompanyBankAccount> getBankAccounts(Company company) {
        return company.getBankAccounts();
    }

    // ==================== Mapper Methods ====================

    private CompanyRequest mapToCompanyRequest(CompanyInput input) {
        return CompanyRequest.builder()
                .tenantId(input.getTenantId())
                .code(input.getCode())
                .name(input.getName())
                .shortName(input.getShortName())
                .industry(input.getIndustry())
                .industryDescription(input.getIndustryDescription())
                .companyType(input.getCompanyType())
                .logo(input.getLogoUrl())
                .addressLine1(input.getAddressLine1())
                .addressLine2(input.getAddressLine2())
                .country(input.getCountry())
                .state(input.getState())
                .city(input.getCity())
                .pincode(input.getPincode())
                .primaryPhone(input.getPrimaryPhone())
                .alternatePhone(input.getAlternatePhone())
                .email(input.getEmail())
                .website(input.getWebsite())
                .contactName(input.getContactName())
                .contactDesignation(input.getContactDesignation())
                .contactEmail(input.getContactEmail())
                .contactPhone(input.getContactPhone())
                .adminNotes(input.getAdminNotes())
                .isActive(input.getIsActive() != null ? input.getIsActive() : true)
                .build();
    }

    private CompanyStatutoryRequest mapToStatutoryRequest(CompanyStatutoryInput input) {
        return CompanyStatutoryRequest.builder()
                .pan(input.getPan())
                .tan(input.getTan())
                .cin(input.getCin())
                .lin(input.getLin())
                .gstin(input.getGstin())
                .pfEnabled(input.getPfEnabled())
                .pfAccountNumber(input.getPfAccountNumber())
                .pfCeiling(input.getPfCeiling())
                .pfEmployeeRate(input.getPfEmployeeRate())
                .pfEmployerRate(input.getPfEmployerRate())
                .pfEmployerEpfRate(input.getPfEmployerEpfRate())
                .pfEmployerEpsRate(input.getPfEmployerEpsRate())
                .esiEnabled(input.getEsiEnabled())
                .esiNumber(input.getEsiNumber())
                .esiCeiling(input.getEsiCeiling())
                .esiEmployeeRate(input.getEsiEmployeeRate())
                .esiEmployerRate(input.getEsiEmployerRate())
                .ptEnabled(input.getPtEnabled())
                .ptState(input.getPtState())
                .ptRegistrationNumber(input.getPtRegistrationNumber())
                .ptRegistrationDate(input.getPtRegistrationDate())
                .ptValidUpto(input.getPtValidUpto())
                .retirementAge(input.getRetirementAge())
                .tdsType(input.getTdsType())
                .allowTdsOverride(input.getAllowTdsOverride())
                .applicableActs(input.getApplicableActs() != null ? java.util.List.of(input.getApplicableActs()) : null)
                .build();
    }

    private CompanyGeneralSettingsRequest mapToGeneralSettingsRequest(CompanyGeneralSettingsInput input) {
        return CompanyGeneralSettingsRequest.builder()
                .enableDivisions(input.getEnableDivisions())
                .enableDepartment(input.getEnableDepartment())
                .enableSection(input.getEnableSection())
                .enableGrade(input.getEnableGrade())
                .currency(input.getCurrency())
                .dateFormat(input.getDateFormat())
                .timeZone(input.getTimeZone())
                .financialYearStart(input.getFinancialYearStart())
                .language(input.getLanguage())
                .build();
    }

    private CompanyLocationRequest mapToLocationRequest(CompanyLocationInput input) {
        return CompanyLocationRequest.builder()
                .type(input.getType())
                .name(input.getName())
                .code(input.getCode())
                .addressLine1(input.getAddressLine1())
                .addressLine2(input.getAddressLine2())
                .state(input.getState())
                .city(input.getCity())
                .pincode(input.getPincode())
                .esiNumber(input.getEsiNumber())
                .pfNumber(input.getPfNumber())
                .ptNumber(input.getPtNumber())
                .gstin(input.getGstin())
                .licenseNumber(input.getLicenseNumber())
                .contactName(input.getContactName())
                .contactPhone(input.getContactPhone())
                .contactEmail(input.getContactEmail())
                .isActive(input.getIsActive() != null ? input.getIsActive() : true)
                .build();
    }

    private CompanyBankAccountRequest mapToBankAccountRequest(CompanyBankAccountInput input) {
        return CompanyBankAccountRequest.builder()
                .beneficiaryName(input.getBeneficiaryName())
                .accountName(input.getAccountName())
                .bankName(input.getBankName())
                .branchName(input.getBranchName())
                .accountNumber(input.getAccountNumber())
                .ifscCode(input.getIfscCode())
                .accountType(input.getAccountType())
                .isPrimary(input.getIsPrimary() != null ? input.getIsPrimary() : false)
                .isActive(input.getIsActive() != null ? input.getIsActive() : true)
                .build();
    }
}
