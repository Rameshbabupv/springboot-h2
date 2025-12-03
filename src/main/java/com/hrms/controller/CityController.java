package com.hrms.controller;

import com.hrms.dto.request.CityRequest;
import com.hrms.dto.response.ApiResponse;
import com.hrms.entity.City;
import com.hrms.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for City operations.
 */
@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@Tag(name = "City", description = "City management APIs")
public class CityController {

    private final CityService cityService;

    @GetMapping
    @Operation(summary = "Get all cities")
    public ResponseEntity<ApiResponse<List<City>>> getAllCities() {
        List<City> cities = cityService.getAllCities();
        return ResponseEntity.ok(ApiResponse.success("Cities fetched successfully", cities));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get city by ID")
    public ResponseEntity<ApiResponse<City>> getCityById(@PathVariable Long id) {
        City city = cityService.getCityById(id);
        return ResponseEntity.ok(ApiResponse.success("City fetched successfully", city));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get cities by tenant ID")
    public ResponseEntity<ApiResponse<List<City>>> getCitiesByTenant(@PathVariable String tenantId) {
        List<City> cities = cityService.getCitiesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Cities fetched successfully", cities));
    }

    @GetMapping("/tenant/{tenantId}/active")
    @Operation(summary = "Get active cities by tenant ID")
    public ResponseEntity<ApiResponse<List<City>>> getActiveCitiesByTenant(@PathVariable String tenantId) {
        List<City> cities = cityService.getActiveCitiesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Active cities fetched successfully", cities));
    }

    @GetMapping("/tenant/{tenantId}/search")
    @Operation(summary = "Search cities by tenant ID")
    public ResponseEntity<ApiResponse<List<City>>> searchCities(
            @PathVariable String tenantId,
            @RequestParam(required = false) String searchTerm) {
        List<City> cities = cityService.searchCities(tenantId, searchTerm);
        return ResponseEntity.ok(ApiResponse.success("Cities search completed successfully", cities));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active cities")
    public ResponseEntity<ApiResponse<List<City>>> getActiveCities() {
        List<City> cities = cityService.getActiveCities();
        return ResponseEntity.ok(ApiResponse.success("Active cities fetched successfully", cities));
    }

    @GetMapping("/state/{stateId}")
    @Operation(summary = "Get cities by state ID (cascading dropdown)")
    public ResponseEntity<ApiResponse<List<City>>> getCitiesByState(@PathVariable Long stateId) {
        List<City> cities = cityService.getCitiesByState(stateId);
        return ResponseEntity.ok(ApiResponse.success("Cities fetched successfully", cities));
    }

    @GetMapping("/tenant/{tenantId}/state/{stateId}")
    @Operation(summary = "Get cities by tenant ID and state ID")
    public ResponseEntity<ApiResponse<List<City>>> getCitiesByTenantAndState(
            @PathVariable String tenantId,
            @PathVariable Long stateId) {
        List<City> cities = cityService.getCitiesByTenantAndState(tenantId, stateId);
        return ResponseEntity.ok(ApiResponse.success("Cities fetched successfully", cities));
    }

    @GetMapping("/country/{countryId}")
    @Operation(summary = "Get cities by country ID")
    public ResponseEntity<ApiResponse<List<City>>> getCitiesByCountry(@PathVariable Long countryId) {
        List<City> cities = cityService.getCitiesByCountry(countryId);
        return ResponseEntity.ok(ApiResponse.success("Cities fetched successfully", cities));
    }

    @GetMapping("/tenant/{tenantId}/country/{countryId}")
    @Operation(summary = "Get cities by tenant ID and country ID")
    public ResponseEntity<ApiResponse<List<City>>> getCitiesByTenantAndCountry(
            @PathVariable String tenantId,
            @PathVariable Long countryId) {
        List<City> cities = cityService.getCitiesByTenantAndCountry(tenantId, countryId);
        return ResponseEntity.ok(ApiResponse.success("Cities fetched successfully", cities));
    }

    @PostMapping
    @Operation(summary = "Create a new city")
    public ResponseEntity<ApiResponse<City>> createCity(@Valid @RequestBody CityRequest request) {
        City created = cityService.createCity(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("City created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing city")
    public ResponseEntity<ApiResponse<City>> updateCity(
            @PathVariable Long id,
            @Valid @RequestBody CityRequest request) {
        City updated = cityService.updateCity(id, request);
        return ResponseEntity.ok(ApiResponse.success("City updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a city")
    public ResponseEntity<ApiResponse<Void>> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity.ok(ApiResponse.success("City deleted successfully", null));
    }
}
