package com.hrms.controller;

import com.hrms.dto.request.CountryRequest;
import com.hrms.dto.response.ApiResponse;
import com.hrms.entity.Country;
import com.hrms.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Country operations.
 */
@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
@Tag(name = "Country", description = "Country management APIs")
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    @Operation(summary = "Get all countries")
    public ResponseEntity<ApiResponse<List<Country>>> getAllCountries() {
        List<Country> countries = countryService.getAllCountries();
        return ResponseEntity.ok(ApiResponse.success("Countries fetched successfully", countries));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get country by ID")
    public ResponseEntity<ApiResponse<Country>> getCountryById(@PathVariable Long id) {
        Country country = countryService.getCountryById(id);
        return ResponseEntity.ok(ApiResponse.success("Country fetched successfully", country));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get countries by tenant ID")
    public ResponseEntity<ApiResponse<List<Country>>> getCountriesByTenant(@PathVariable String tenantId) {
        List<Country> countries = countryService.getCountriesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Countries fetched successfully", countries));
    }

    @GetMapping("/tenant/{tenantId}/active")
    @Operation(summary = "Get active countries by tenant ID")
    public ResponseEntity<ApiResponse<List<Country>>> getActiveCountriesByTenant(@PathVariable String tenantId) {
        List<Country> countries = countryService.getActiveCountriesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Active countries fetched successfully", countries));
    }

    @GetMapping("/tenant/{tenantId}/search")
    @Operation(summary = "Search countries by tenant ID")
    public ResponseEntity<ApiResponse<List<Country>>> searchCountries(
            @PathVariable String tenantId,
            @RequestParam(required = false) String searchTerm) {
        List<Country> countries = countryService.searchCountries(tenantId, searchTerm);
        return ResponseEntity.ok(ApiResponse.success("Countries search completed successfully", countries));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active countries")
    public ResponseEntity<ApiResponse<List<Country>>> getActiveCountries() {
        List<Country> countries = countryService.getActiveCountries();
        return ResponseEntity.ok(ApiResponse.success("Active countries fetched successfully", countries));
    }

    @PostMapping
    @Operation(summary = "Create a new country")
    public ResponseEntity<ApiResponse<Country>> createCountry(@Valid @RequestBody CountryRequest request) {
        Country created = countryService.createCountry(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Country created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing country")
    public ResponseEntity<ApiResponse<Country>> updateCountry(
            @PathVariable Long id,
            @Valid @RequestBody CountryRequest request) {
        Country updated = countryService.updateCountry(id, request);
        return ResponseEntity.ok(ApiResponse.success("Country updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a country")
    public ResponseEntity<ApiResponse<Void>> deleteCountry(@PathVariable Long id) {
        countryService.deleteCountry(id);
        return ResponseEntity.ok(ApiResponse.success("Country deleted successfully", null));
    }
}
