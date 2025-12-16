package com.hrms.graphql.resolver;

import com.hrms.dto.request.*;
import com.hrms.entity.*;
import com.hrms.graphql.input.*;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.CompanyService;
import com.hrms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CompanyResolver {

    private final CompanyService companyService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    // ==================== Company Queries ====================

    @QueryMapping
    public List<Company> companies() {
        log.debug("GraphQL Query: companies");
        List<Company> result = companyService.getAllCompanies();
        log.info("GraphQL Response: companies\n{}", LoggingUtil.formatListForLog(result));
        return result;
    }

    @QueryMapping
    public Company company(@Argument Long id) {
        log.debug("GraphQL Query: company - id: {}", id);
        Company result = companyService.getCompanyById(id);
        log.info("GraphQL Response: company - returned company: {} ({})", result.getName(), result.getCode());
        return result;
    }

    @QueryMapping
    public Company companyByCode(@Argument String code) {
        log.debug("GraphQL Query: companyByCode - code: {}", code);
        Company result = companyService.getCompanyByCode(code);
        log.info("GraphQL Response: companyByCode - returned company: {}", result.getName());
        return result;
    }

    @QueryMapping
    public List<Company> companiesByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: companiesByTenant - tenantId: {}", tenantId);
        List<Company> result = companyService.getCompaniesByTenant(tenantId);
        log.info("GraphQL Response: companiesByTenant - returned {} companies for tenant: {}", result.size(), tenantId);
        return result;
    }

    @QueryMapping
    public List<Company> activeCompanies() {
        log.debug("GraphQL Query: activeCompanies");
        List<Company> result = companyService.getActiveCompanies();
        log.info("GraphQL Response: activeCompanies - returned {} active companies", result.size());
        return result;
    }

    // ==================== Company Mutations ====================

    @MutationMapping
    public Company createCompany(@Argument CompanyInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);

        log.debug("GraphQL Mutation: createCompany - name: {}, code: {}, tenantId: {}",
                  input.getName(), input.getCode(), tenantId);

        CompanyRequest request = mapToCompanyRequest(input, tenantId);
        Company result = companyService.createCompany(request);

        log.info("GraphQL Response: createCompany - created company id: {}, name: {}, code: {}",
                 result.getId(), result.getName(), result.getCode());
        return result;
    }

    @MutationMapping
    public Company updateCompany(@Argument Long id, @Argument CompanyInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);

        log.debug("GraphQL Mutation: updateCompany - id: {}, name: {}", id, input.getName());
        CompanyRequest request = mapToCompanyRequest(input, tenantId);
        Company result = companyService.updateCompany(id, request);
        log.info("GraphQL Response: updateCompany - updated company id: {}, name: {}", result.getId(), result.getName());
        return result;
    }

    @MutationMapping
    public Boolean deleteCompany(@Argument Long id) {
        log.debug("GraphQL Mutation: deleteCompany - id: {}", id);
        companyService.deleteCompany(id);
        log.info("GraphQL Response: deleteCompany - successfully deleted company id: {}", id);
        return true;
    }

    // ==================== Statutory Queries/Mutations ====================

    @QueryMapping
    public CompanyStatutory companyStatutory(@Argument Long companyId) {
        log.debug("GraphQL Query: companyStatutory - companyId: {}", companyId);
        CompanyStatutory result = companyService.getStatutoryByCompanyId(companyId);
        log.info("GraphQL Response: companyStatutory - returned statutory info for company: {}", companyId);
        return result;
    }

    @MutationMapping
    public CompanyStatutory updateCompanyStatutory(@Argument Long companyId, @Argument CompanyStatutoryInput input) {
        log.debug("GraphQL Mutation: updateCompanyStatutory - companyId: {}", companyId);
        CompanyStatutoryRequest request = mapToStatutoryRequest(input);
        CompanyStatutory result = companyService.updateStatutory(companyId, request);
        log.info("GraphQL Response: updateCompanyStatutory - updated statutory for company: {}", companyId);
        return result;
    }

    // ==================== General Settings Queries/Mutations ====================

    @QueryMapping
    public CompanyGeneralSettings companyGeneralSettings(@Argument Long companyId) {
        log.debug("GraphQL Query: companyGeneralSettings - companyId: {}", companyId);
        CompanyGeneralSettings result = companyService.getGeneralSettingsByCompanyId(companyId);
        log.info("GraphQL Response: companyGeneralSettings - returned settings for company: {}", companyId);
        return result;
    }

    @MutationMapping
    public CompanyGeneralSettings updateCompanyGeneralSettings(@Argument Long companyId, @Argument CompanyGeneralSettingsInput input) {
        log.debug("GraphQL Mutation: updateCompanyGeneralSettings - companyId: {}", companyId);
        CompanyGeneralSettingsRequest request = mapToGeneralSettingsRequest(input);
        CompanyGeneralSettings result = companyService.updateGeneralSettings(companyId, request);
        log.info("GraphQL Response: updateCompanyGeneralSettings - updated settings for company: {}", companyId);
        return result;
    }

    // ==================== Location Queries/Mutations ====================

    @QueryMapping
    public List<CompanyLocation> companyLocations(@Argument Long companyId) {
        log.debug("GraphQL Query: companyLocations - companyId: {}", companyId);
        List<CompanyLocation> result = companyService.getLocationsByCompanyId(companyId);
        log.info("GraphQL Response: companyLocations - returned {} locations for company: {}", result.size(), companyId);
        return result;
    }

    @QueryMapping
    public CompanyLocation companyLocation(@Argument Long id) {
        log.debug("GraphQL Query: companyLocation - id: {}", id);
        CompanyLocation result = companyService.getLocationById(id);
        log.info("GraphQL Response: companyLocation - returned location: {}", result.getName());
        return result;
    }

    @MutationMapping
    public CompanyLocation createCompanyLocation(@Argument Long companyId, @Argument CompanyLocationInput input) {
        log.debug("GraphQL Mutation: createCompanyLocation - companyId: {}, name: {}", companyId, input.getName());
        CompanyLocationRequest request = mapToLocationRequest(input);
        CompanyLocation result = companyService.createLocation(companyId, request);
        log.info("GraphQL Response: createCompanyLocation - created location id: {}, name: {}", result.getId(), result.getName());
        return result;
    }

    @MutationMapping
    public CompanyLocation updateCompanyLocation(@Argument Long id, @Argument CompanyLocationInput input) {
        log.debug("GraphQL Mutation: updateCompanyLocation - id: {}", id);
        CompanyLocationRequest request = mapToLocationRequest(input);
        CompanyLocation result = companyService.updateLocation(id, request);
        log.info("GraphQL Response: updateCompanyLocation - updated location id: {}", result.getId());
        return result;
    }

    @MutationMapping
    public Boolean deleteCompanyLocation(@Argument Long id) {
        log.debug("GraphQL Mutation: deleteCompanyLocation - id: {}", id);
        companyService.deleteLocation(id);
        log.info("GraphQL Response: deleteCompanyLocation - successfully deleted location id: {}", id);
        return true;
    }

    // ==================== Bank Account Queries/Mutations ====================

    @QueryMapping
    public List<CompanyBankAccount> companyBankAccounts(@Argument Long companyId) {
        log.debug("GraphQL Query: companyBankAccounts - companyId: {}", companyId);
        List<CompanyBankAccount> result = companyService.getBankAccountsByCompanyId(companyId);
        log.info("GraphQL Response: companyBankAccounts - returned {} bank accounts for company: {}", result.size(), companyId);
        return result;
    }

    @QueryMapping
    public CompanyBankAccount companyBankAccount(@Argument Long id) {
        log.debug("GraphQL Query: companyBankAccount - id: {}", id);
        CompanyBankAccount result = companyService.getBankAccountById(id);
        log.info("GraphQL Response: companyBankAccount - returned bank account: {}", result.getAccountName());
        return result;
    }

    @MutationMapping
    public CompanyBankAccount createCompanyBankAccount(@Argument Long companyId, @Argument CompanyBankAccountInput input) {
        log.debug("GraphQL Mutation: createCompanyBankAccount - companyId: {}, accountName: {}", companyId, input.getAccountName());
        CompanyBankAccountRequest request = mapToBankAccountRequest(input);
        CompanyBankAccount result = companyService.createBankAccount(companyId, request);
        log.info("GraphQL Response: createCompanyBankAccount - created bank account id: {}", result.getId());
        return result;
    }

    @MutationMapping
    public CompanyBankAccount updateCompanyBankAccount(@Argument Long id, @Argument CompanyBankAccountInput input) {
        log.debug("GraphQL Mutation: updateCompanyBankAccount - id: {}", id);
        CompanyBankAccountRequest request = mapToBankAccountRequest(input);
        CompanyBankAccount result = companyService.updateBankAccount(id, request);
        log.info("GraphQL Response: updateCompanyBankAccount - updated bank account id: {}", result.getId());
        return result;
    }

    @MutationMapping
    public Boolean deleteCompanyBankAccount(@Argument Long id) {
        log.debug("GraphQL Mutation: deleteCompanyBankAccount - id: {}", id);
        companyService.deleteBankAccount(id);
        log.info("GraphQL Response: deleteCompanyBankAccount - successfully deleted bank account id: {}", id);
        return true;
    }

    @MutationMapping
    public CompanyBankAccount setCompanyPrimaryBankAccount(@Argument Long id) {
        log.debug("GraphQL Mutation: setCompanyPrimaryBankAccount - id: {}", id);
        CompanyBankAccount result = companyService.setPrimaryBankAccount(id);
        log.info("GraphQL Response: setCompanyPrimaryBankAccount - set primary bank account id: {}", id);
        return result;
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

    private CompanyRequest mapToCompanyRequest(CompanyInput input, String tenantId) {
        return CompanyRequest.builder()
                .tenantId(tenantId)
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
