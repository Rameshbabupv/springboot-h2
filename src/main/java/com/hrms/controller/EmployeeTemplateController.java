package com.hrms.controller;

import com.hrms.dto.request.EmployeeTemplateRequest;
import com.hrms.dto.request.TemplateCriteriaRequest;
import com.hrms.dto.response.ApiResponse;
import com.hrms.dto.response.EmployeeTemplateResponse;
import com.hrms.service.EmployeeTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee-templates")
@RequiredArgsConstructor
@Tag(name = "Employee Template", description = "Employee Template Configuration APIs")
public class EmployeeTemplateController {

    private final EmployeeTemplateService templateService;

    @GetMapping
    @Operation(summary = "Get all employee templates for a tenant")
    public ResponseEntity<ApiResponse<List<EmployeeTemplateResponse>>> getAllTemplates(
            @RequestParam String tenantId) {
        List<EmployeeTemplateResponse> templates = templateService.getAllTemplates(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Templates fetched successfully", templates));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active employee templates for a tenant")
    public ResponseEntity<ApiResponse<List<EmployeeTemplateResponse>>> getActiveTemplates(
            @RequestParam String tenantId) {
        List<EmployeeTemplateResponse> templates = templateService.getActiveTemplates(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Active templates fetched successfully", templates));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee template by ID")
    public ResponseEntity<ApiResponse<EmployeeTemplateResponse>> getTemplateById(@PathVariable Long id) {
        EmployeeTemplateResponse template = templateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success("Template fetched successfully", template));
    }

    @PostMapping
    @Operation(summary = "Create a new employee template")
    public ResponseEntity<ApiResponse<EmployeeTemplateResponse>> createTemplate(
            @Valid @RequestBody EmployeeTemplateRequest request) {
        EmployeeTemplateResponse created = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Template created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing employee template")
    public ResponseEntity<ApiResponse<EmployeeTemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeTemplateRequest request) {
        EmployeeTemplateResponse updated = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Template updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee template")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted successfully", null));
    }

    @PostMapping("/get-applicable")
    @Operation(summary = "Get applicable template based on criteria")
    public ResponseEntity<ApiResponse<EmployeeTemplateResponse>> getApplicableTemplate(
            @Valid @RequestBody TemplateCriteriaRequest criteria) {
        EmployeeTemplateResponse template = templateService.getApplicableTemplate(criteria);
        return ResponseEntity.ok(ApiResponse.success("Applicable template fetched successfully", template));
    }

    @PostMapping("/get-all-applicable")
    @Operation(summary = "Get all applicable templates based on criteria")
    public ResponseEntity<ApiResponse<List<EmployeeTemplateResponse>>> getApplicableTemplates(
            @Valid @RequestBody TemplateCriteriaRequest criteria) {
        List<EmployeeTemplateResponse> templates = templateService.getApplicableTemplates(criteria);
        return ResponseEntity.ok(ApiResponse.success("Applicable templates fetched successfully", templates));
    }
}
