package com.hrms.controller;

import com.hrms.dto.request.*;
import com.hrms.dto.response.ApiResponse;
import com.hrms.entity.*;
import com.hrms.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Company operations.
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Company", description = "Company management APIs")
public class CompanyController {

    private final CompanyService companyService;

    // ==================== Company CRUD ====================

    @GetMapping
    @Operation(summary = "Get all companies")
    public ResponseEntity<ApiResponse<List<Company>>> getAllCompanies() {
        List<Company> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(ApiResponse.success("Companies fetched successfully", companies));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company by ID")
    public ResponseEntity<ApiResponse<Company>> getCompanyById(@PathVariable Long id) {
        Company company = companyService.getCompanyById(id);
        return ResponseEntity.ok(ApiResponse.success("Company fetched successfully", company));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get company by code")
    public ResponseEntity<ApiResponse<Company>> getCompanyByCode(@PathVariable String code) {
        Company company = companyService.getCompanyByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Company fetched successfully", company));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get companies by tenant ID")
    public ResponseEntity<ApiResponse<List<Company>>> getCompaniesByTenant(@PathVariable String tenantId) {
        List<Company> companies = companyService.getCompaniesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Companies fetched successfully", companies));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active companies")
    public ResponseEntity<ApiResponse<List<Company>>> getActiveCompanies() {
        List<Company> companies = companyService.getActiveCompanies();
        return ResponseEntity.ok(ApiResponse.success("Active companies fetched successfully", companies));
    }

    @PostMapping
    @Operation(summary = "Create a new company")
    public ResponseEntity<ApiResponse<Company>> createCompany(@Valid @RequestBody CompanyRequest request) {
        Company created = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Company created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing company")
    public ResponseEntity<ApiResponse<Company>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request) {
        Company updated = companyService.updateCompany(id, request);
        return ResponseEntity.ok(ApiResponse.success("Company updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a company")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(ApiResponse.success("Company deleted successfully", null));
    }

    // ==================== Statutory ====================

    @GetMapping("/{companyId}/statutory")
    @Operation(summary = "Get statutory details for a company")
    public ResponseEntity<ApiResponse<CompanyStatutory>> getStatutory(@PathVariable Long companyId) {
        CompanyStatutory statutory = companyService.getStatutoryByCompanyId(companyId);
        return ResponseEntity.ok(ApiResponse.success("Statutory details fetched successfully", statutory));
    }

    @PutMapping("/{companyId}/statutory")
    @Operation(summary = "Update statutory details for a company")
    public ResponseEntity<ApiResponse<CompanyStatutory>> updateStatutory(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyStatutoryRequest request) {
        CompanyStatutory updated = companyService.updateStatutory(companyId, request);
        return ResponseEntity.ok(ApiResponse.success("Statutory details updated successfully", updated));
    }

    // ==================== General Settings ====================

    @GetMapping("/{companyId}/settings")
    @Operation(summary = "Get general settings for a company")
    public ResponseEntity<ApiResponse<CompanyGeneralSettings>> getGeneralSettings(@PathVariable Long companyId) {
        CompanyGeneralSettings settings = companyService.getGeneralSettingsByCompanyId(companyId);
        return ResponseEntity.ok(ApiResponse.success("General settings fetched successfully", settings));
    }

    @PutMapping("/{companyId}/settings")
    @Operation(summary = "Update general settings for a company")
    public ResponseEntity<ApiResponse<CompanyGeneralSettings>> updateGeneralSettings(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyGeneralSettingsRequest request) {
        CompanyGeneralSettings updated = companyService.updateGeneralSettings(companyId, request);
        return ResponseEntity.ok(ApiResponse.success("General settings updated successfully", updated));
    }

    // ==================== Locations ====================

    @GetMapping("/{companyId}/locations")
    @Operation(summary = "Get all locations for a company")
    public ResponseEntity<ApiResponse<List<CompanyLocation>>> getLocations(@PathVariable Long companyId) {
        List<CompanyLocation> locations = companyService.getLocationsByCompanyId(companyId);
        return ResponseEntity.ok(ApiResponse.success("Locations fetched successfully", locations));
    }

    @GetMapping("/locations/{id}")
    @Operation(summary = "Get location by ID")
    public ResponseEntity<ApiResponse<CompanyLocation>> getLocationById(@PathVariable Long id) {
        CompanyLocation location = companyService.getLocationById(id);
        return ResponseEntity.ok(ApiResponse.success("Location fetched successfully", location));
    }

    @PostMapping("/{companyId}/locations")
    @Operation(summary = "Create a new location for a company")
    public ResponseEntity<ApiResponse<CompanyLocation>> createLocation(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyLocationRequest request) {
        CompanyLocation created = companyService.createLocation(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Location created successfully", created));
    }

    @PutMapping("/locations/{id}")
    @Operation(summary = "Update a location")
    public ResponseEntity<ApiResponse<CompanyLocation>> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody CompanyLocationRequest request) {
        CompanyLocation updated = companyService.updateLocation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", updated));
    }

    @DeleteMapping("/locations/{id}")
    @Operation(summary = "Delete a location")
    public ResponseEntity<ApiResponse<Void>> deleteLocation(@PathVariable Long id) {
        companyService.deleteLocation(id);
        return ResponseEntity.ok(ApiResponse.success("Location deleted successfully", null));
    }

    // ==================== Bank Accounts ====================

    @GetMapping("/{companyId}/bank-accounts")
    @Operation(summary = "Get all bank accounts for a company")
    public ResponseEntity<ApiResponse<List<CompanyBankAccount>>> getBankAccounts(@PathVariable Long companyId) {
        List<CompanyBankAccount> accounts = companyService.getBankAccountsByCompanyId(companyId);
        return ResponseEntity.ok(ApiResponse.success("Bank accounts fetched successfully", accounts));
    }

    @GetMapping("/bank-accounts/{id}")
    @Operation(summary = "Get bank account by ID")
    public ResponseEntity<ApiResponse<CompanyBankAccount>> getBankAccountById(@PathVariable Long id) {
        CompanyBankAccount account = companyService.getBankAccountById(id);
        return ResponseEntity.ok(ApiResponse.success("Bank account fetched successfully", account));
    }

    @PostMapping("/{companyId}/bank-accounts")
    @Operation(summary = "Create a new bank account for a company")
    public ResponseEntity<ApiResponse<CompanyBankAccount>> createBankAccount(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyBankAccountRequest request) {
        CompanyBankAccount created = companyService.createBankAccount(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bank account created successfully", created));
    }

    @PutMapping("/bank-accounts/{id}")
    @Operation(summary = "Update a bank account")
    public ResponseEntity<ApiResponse<CompanyBankAccount>> updateBankAccount(
            @PathVariable Long id,
            @Valid @RequestBody CompanyBankAccountRequest request) {
        CompanyBankAccount updated = companyService.updateBankAccount(id, request);
        return ResponseEntity.ok(ApiResponse.success("Bank account updated successfully", updated));
    }

    @DeleteMapping("/bank-accounts/{id}")
    @Operation(summary = "Delete a bank account")
    public ResponseEntity<ApiResponse<Void>> deleteBankAccount(@PathVariable Long id) {
        companyService.deleteBankAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Bank account deleted successfully", null));
    }

    @PutMapping("/bank-accounts/{id}/set-primary")
    @Operation(summary = "Set a bank account as primary")
    public ResponseEntity<ApiResponse<CompanyBankAccount>> setPrimaryBankAccount(@PathVariable Long id) {
        CompanyBankAccount updated = companyService.setPrimaryBankAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Bank account set as primary successfully", updated));
    }
}
