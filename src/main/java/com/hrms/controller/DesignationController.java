package com.hrms.controller;

import com.hrms.dto.request.DesignationRequest;
import com.hrms.dto.response.ApiResponse;
import com.hrms.entity.Designation;
import com.hrms.service.DesignationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Designation operations.
 */
@RestController
@RequestMapping("/api/designations")
@RequiredArgsConstructor
@Tag(name = "Designation", description = "Designation management APIs")
public class DesignationController {

    private final DesignationService designationService;

    @GetMapping
    @Operation(summary = "Get all designations")
    public ResponseEntity<ApiResponse<List<Designation>>> getAllDesignations() {
        List<Designation> designations = designationService.getAllDesignations();
        return ResponseEntity.ok(ApiResponse.success("Designations fetched successfully", designations));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get designation by ID")
    public ResponseEntity<ApiResponse<Designation>> getDesignationById(@PathVariable Long id) {
        Designation designation = designationService.getDesignationById(id);
        return ResponseEntity.ok(ApiResponse.success("Designation fetched successfully", designation));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get designations by tenant ID")
    public ResponseEntity<ApiResponse<List<Designation>>> getDesignationsByTenant(@PathVariable String tenantId) {
        List<Designation> designations = designationService.getDesignationsByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Designations fetched successfully", designations));
    }

    @GetMapping("/tenant/{tenantId}/active")
    @Operation(summary = "Get active designations by tenant ID")
    public ResponseEntity<ApiResponse<List<Designation>>> getActiveDesignationsByTenant(@PathVariable String tenantId) {
        List<Designation> designations = designationService.getActiveDesignationsByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Active designations fetched successfully", designations));
    }

    @GetMapping("/tenant/{tenantId}/search")
    @Operation(summary = "Search designations by tenant ID")
    public ResponseEntity<ApiResponse<List<Designation>>> searchDesignations(
            @PathVariable String tenantId,
            @RequestParam(required = false) String searchTerm) {
        List<Designation> designations = designationService.searchDesignations(tenantId, searchTerm);
        return ResponseEntity.ok(ApiResponse.success("Designations search completed successfully", designations));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active designations")
    public ResponseEntity<ApiResponse<List<Designation>>> getActiveDesignations() {
        List<Designation> designations = designationService.getActiveDesignations();
        return ResponseEntity.ok(ApiResponse.success("Active designations fetched successfully", designations));
    }

    @PostMapping
    @Operation(summary = "Create a new designation")
    public ResponseEntity<ApiResponse<Designation>> createDesignation(@Valid @RequestBody DesignationRequest request) {
        Designation created = designationService.createDesignation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Designation created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing designation")
    public ResponseEntity<ApiResponse<Designation>> updateDesignation(
            @PathVariable Long id,
            @Valid @RequestBody DesignationRequest request) {
        Designation updated = designationService.updateDesignation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Designation updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a designation")
    public ResponseEntity<ApiResponse<Void>> deleteDesignation(@PathVariable Long id) {
        designationService.deleteDesignation(id);
        return ResponseEntity.ok(ApiResponse.success("Designation deleted successfully", null));
    }
}
