package com.hrms.controller;

import com.hrms.dto.request.DivisionRequest;
import com.hrms.dto.response.ApiResponse;
import com.hrms.entity.Division;
import com.hrms.service.DivisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Division operations.
 */
@RestController
@RequestMapping("/api/divisions")
@RequiredArgsConstructor
@Tag(name = "Division", description = "Division management APIs")
public class DivisionController {

    private final DivisionService divisionService;

    @GetMapping
    @Operation(summary = "Get all divisions")
    public ResponseEntity<ApiResponse<List<Division>>> getAllDivisions() {
        List<Division> divisions = divisionService.getAllDivisions();
        return ResponseEntity.ok(ApiResponse.success("Divisions fetched successfully", divisions));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get division by ID")
    public ResponseEntity<ApiResponse<Division>> getDivisionById(@PathVariable Long id) {
        Division division = divisionService.getDivisionById(id);
        return ResponseEntity.ok(ApiResponse.success("Division fetched successfully", division));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get divisions by tenant ID")
    public ResponseEntity<ApiResponse<List<Division>>> getDivisionsByTenant(@PathVariable String tenantId) {
        List<Division> divisions = divisionService.getDivisionsByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Divisions fetched successfully", divisions));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active divisions")
    public ResponseEntity<ApiResponse<List<Division>>> getActiveDivisions() {
        List<Division> divisions = divisionService.getActiveDivisions();
        return ResponseEntity.ok(ApiResponse.success("Active divisions fetched successfully", divisions));
    }

    @PostMapping
    @Operation(summary = "Create a new division")
    public ResponseEntity<ApiResponse<Division>> createDivision(@Valid @RequestBody DivisionRequest request) {
        Division created = divisionService.createDivision(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Division created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing division")
    public ResponseEntity<ApiResponse<Division>> updateDivision(
            @PathVariable Long id,
            @Valid @RequestBody DivisionRequest request) {
        Division updated = divisionService.updateDivision(id, request);
        return ResponseEntity.ok(ApiResponse.success("Division updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a division")
    public ResponseEntity<ApiResponse<Void>> deleteDivision(@PathVariable Long id) {
        divisionService.deleteDivision(id);
        return ResponseEntity.ok(ApiResponse.success("Division deleted successfully", null));
    }
}
