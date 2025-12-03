package com.hrms.service;

import com.hrms.dto.request.*;
import com.hrms.entity.*;

import java.util.List;

/**
 * Service interface for Company operations.
 */
public interface CompanyService {

    // Company CRUD
    List<Company> getAllCompanies();
    Company getCompanyById(Long id);
    Company getCompanyByCode(String code);
    List<Company> getCompaniesByTenant(String tenantId);
    List<Company> getActiveCompanies();
    Company createCompany(CompanyRequest request);
    Company updateCompany(Long id, CompanyRequest request);
    void deleteCompany(Long id);
    boolean existsById(Long id);

    // Statutory
    CompanyStatutory getStatutoryByCompanyId(Long companyId);
    CompanyStatutory updateStatutory(Long companyId, CompanyStatutoryRequest request);

    // General Settings
    CompanyGeneralSettings getGeneralSettingsByCompanyId(Long companyId);
    CompanyGeneralSettings updateGeneralSettings(Long companyId, CompanyGeneralSettingsRequest request);

    // Locations
    List<CompanyLocation> getLocationsByCompanyId(Long companyId);
    CompanyLocation getLocationById(Long id);
    CompanyLocation createLocation(Long companyId, CompanyLocationRequest request);
    CompanyLocation updateLocation(Long id, CompanyLocationRequest request);
    void deleteLocation(Long id);

    // Bank Accounts
    List<CompanyBankAccount> getBankAccountsByCompanyId(Long companyId);
    CompanyBankAccount getBankAccountById(Long id);
    CompanyBankAccount createBankAccount(Long companyId, CompanyBankAccountRequest request);
    CompanyBankAccount updateBankAccount(Long id, CompanyBankAccountRequest request);
    void deleteBankAccount(Long id);
    CompanyBankAccount setPrimaryBankAccount(Long id);
}
