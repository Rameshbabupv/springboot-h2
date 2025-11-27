package com.hrms.controller;

import com.hrms.dto.request.FieldDefinitionMasterRequest;
import com.hrms.dto.response.ApiResponse;
import com.hrms.dto.response.FieldDefinitionMasterResponse;
import com.hrms.service.FieldDefinitionMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/field-definitions")
@RequiredArgsConstructor
@Tag(name = "Field Definition Master", description = "Field Definition Management APIs")
public class FieldDefinitionMasterController {

    private final FieldDefinitionMasterService fieldDefinitionService;

    @GetMapping
    @Operation(summary = "Get all field definitions for a tenant")
    public ResponseEntity<ApiResponse<List<FieldDefinitionMasterResponse>>> getAllFields(
            @RequestParam String tenantId) {
        List<FieldDefinitionMasterResponse> fields = fieldDefinitionService.getAllFields(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Field definitions fetched successfully", fields));
    }

    @GetMapping("/category")
    @Operation(summary = "Get field definitions by category")
    public ResponseEntity<ApiResponse<List<FieldDefinitionMasterResponse>>> getFieldsByCategory(
            @RequestParam String tenantId,
            @RequestParam String category) {
        List<FieldDefinitionMasterResponse> fields = fieldDefinitionService.getFieldsByCategory(tenantId, category);
        return ResponseEntity.ok(ApiResponse.success("Field definitions fetched successfully", fields));
    }

    @GetMapping("/system")
    @Operation(summary = "Get system field definitions")
    public ResponseEntity<ApiResponse<List<FieldDefinitionMasterResponse>>> getSystemFields(
            @RequestParam String tenantId) {
        List<FieldDefinitionMasterResponse> fields = fieldDefinitionService.getSystemFields(tenantId);
        return ResponseEntity.ok(ApiResponse.success("System field definitions fetched successfully", fields));
    }

    @GetMapping("/custom")
    @Operation(summary = "Get custom field definitions")
    public ResponseEntity<ApiResponse<List<FieldDefinitionMasterResponse>>> getCustomFields(
            @RequestParam String tenantId) {
        List<FieldDefinitionMasterResponse> fields = fieldDefinitionService.getCustomFields(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Custom field definitions fetched successfully", fields));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get field definition by ID")
    public ResponseEntity<ApiResponse<FieldDefinitionMasterResponse>> getFieldById(@PathVariable Long id) {
        FieldDefinitionMasterResponse field = fieldDefinitionService.getFieldById(id);
        return ResponseEntity.ok(ApiResponse.success("Field definition fetched successfully", field));
    }

    @PostMapping
    @Operation(summary = "Create a new field definition")
    public ResponseEntity<ApiResponse<FieldDefinitionMasterResponse>> createField(
            @Valid @RequestBody FieldDefinitionMasterRequest request) {
        FieldDefinitionMasterResponse created = fieldDefinitionService.createField(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Field definition created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a field definition")
    public ResponseEntity<ApiResponse<FieldDefinitionMasterResponse>> updateField(
            @PathVariable Long id,
            @Valid @RequestBody FieldDefinitionMasterRequest request) {
        FieldDefinitionMasterResponse updated = fieldDefinitionService.updateField(id, request);
        return ResponseEntity.ok(ApiResponse.success("Field definition updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a custom field definition")
    public ResponseEntity<ApiResponse<Void>> deleteField(@PathVariable Long id) {
        fieldDefinitionService.deleteField(id);
        return ResponseEntity.ok(ApiResponse.success("Field definition deleted successfully", null));
    }
}
