package com.hrms.controller;

import com.hrms.dto.request.EmploymentTypeRequest;
import com.hrms.dto.response.ApiResponse;
import com.hrms.entity.EmploymentType;
import com.hrms.service.EmploymentTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for EmploymentType operations.
 */
@RestController
@RequestMapping("/api/employment-types")
@RequiredArgsConstructor
@Tag(name = "EmploymentType", description = "Employment Type management APIs")
public class EmploymentTypeController {

    private final EmploymentTypeService employmentTypeService;

    @GetMapping
    @Operation(summary = "Get all employment types")
    public ResponseEntity<ApiResponse<List<EmploymentType>>> getAllEmploymentTypes() {
        List<EmploymentType> employmentTypes = employmentTypeService.getAllEmploymentTypes();
        return ResponseEntity.ok(ApiResponse.success("Employment types fetched successfully", employmentTypes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employment type by ID")
    public ResponseEntity<ApiResponse<EmploymentType>> getEmploymentTypeById(@PathVariable Long id) {
        EmploymentType employmentType = employmentTypeService.getEmploymentTypeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employment type fetched successfully", employmentType));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get employment types by tenant ID")
    public ResponseEntity<ApiResponse<List<EmploymentType>>> getEmploymentTypesByTenant(@PathVariable String tenantId) {
        List<EmploymentType> employmentTypes = employmentTypeService.getEmploymentTypesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Employment types fetched successfully", employmentTypes));
    }

    @GetMapping("/tenant/{tenantId}/active")
    @Operation(summary = "Get active employment types by tenant ID")
    public ResponseEntity<ApiResponse<List<EmploymentType>>> getActiveEmploymentTypesByTenant(@PathVariable String tenantId) {
        List<EmploymentType> employmentTypes = employmentTypeService.getActiveEmploymentTypesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Active employment types fetched successfully", employmentTypes));
    }

    @GetMapping("/tenant/{tenantId}/search")
    @Operation(summary = "Search employment types by tenant ID")
    public ResponseEntity<ApiResponse<List<EmploymentType>>> searchEmploymentTypes(
            @PathVariable String tenantId,
            @RequestParam(required = false) String searchTerm) {
        List<EmploymentType> employmentTypes = employmentTypeService.searchEmploymentTypes(tenantId, searchTerm);
        return ResponseEntity.ok(ApiResponse.success("Employment types search completed successfully", employmentTypes));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active employment types")
    public ResponseEntity<ApiResponse<List<EmploymentType>>> getActiveEmploymentTypes() {
        List<EmploymentType> employmentTypes = employmentTypeService.getActiveEmploymentTypes();
        return ResponseEntity.ok(ApiResponse.success("Active employment types fetched successfully", employmentTypes));
    }

    @PostMapping
    @Operation(summary = "Create a new employment type")
    public ResponseEntity<ApiResponse<EmploymentType>> createEmploymentType(@Valid @RequestBody EmploymentTypeRequest request) {
        EmploymentType created = employmentTypeService.createEmploymentType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employment type created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing employment type")
    public ResponseEntity<ApiResponse<EmploymentType>> updateEmploymentType(
            @PathVariable Long id,
            @Valid @RequestBody EmploymentTypeRequest request) {
        EmploymentType updated = employmentTypeService.updateEmploymentType(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employment type updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employment type")
    public ResponseEntity<ApiResponse<Void>> deleteEmploymentType(@PathVariable Long id) {
        employmentTypeService.deleteEmploymentType(id);
        return ResponseEntity.ok(ApiResponse.success("Employment type deleted successfully", null));
    }
}
