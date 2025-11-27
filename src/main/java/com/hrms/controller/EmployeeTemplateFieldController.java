package com.hrms.controller;

import com.hrms.dto.request.EmployeeTemplateFieldRequest;
import com.hrms.dto.request.FieldReorderRequest;
import com.hrms.dto.response.ApiResponse;
import com.hrms.dto.response.EmployeeTemplateFieldResponse;
import com.hrms.service.EmployeeTemplateFieldService;
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
@Tag(name = "Employee Template Field", description = "Template Field Management APIs")
public class EmployeeTemplateFieldController {

    private final EmployeeTemplateFieldService templateFieldService;

    @GetMapping("/sections/{sectionId}/fields")
    @Operation(summary = "Get all fields for a section")
    public ResponseEntity<ApiResponse<List<EmployeeTemplateFieldResponse>>> getFieldsBySectionId(
            @PathVariable Long sectionId) {
        List<EmployeeTemplateFieldResponse> fields = templateFieldService.getFieldsBySectionId(sectionId);
        return ResponseEntity.ok(ApiResponse.success("Fields fetched successfully", fields));
    }

    @GetMapping("/fields/{fieldId}")
    @Operation(summary = "Get field by ID")
    public ResponseEntity<ApiResponse<EmployeeTemplateFieldResponse>> getFieldById(
            @PathVariable Long fieldId) {
        EmployeeTemplateFieldResponse field = templateFieldService.getFieldById(fieldId);
        return ResponseEntity.ok(ApiResponse.success("Field fetched successfully", field));
    }

    @PostMapping("/sections/{sectionId}/fields")
    @Operation(summary = "Add a new field to a section")
    public ResponseEntity<ApiResponse<EmployeeTemplateFieldResponse>> createField(
            @PathVariable Long sectionId,
            @Valid @RequestBody EmployeeTemplateFieldRequest request) {
        request.setSectionId(sectionId);
        EmployeeTemplateFieldResponse created = templateFieldService.createField(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Field created successfully", created));
    }

    @PutMapping("/fields/{fieldId}")
    @Operation(summary = "Update a field")
    public ResponseEntity<ApiResponse<EmployeeTemplateFieldResponse>> updateField(
            @PathVariable Long fieldId,
            @Valid @RequestBody EmployeeTemplateFieldRequest request) {
        EmployeeTemplateFieldResponse updated = templateFieldService.updateField(fieldId, request);
        return ResponseEntity.ok(ApiResponse.success("Field updated successfully", updated));
    }

    @DeleteMapping("/fields/{fieldId}")
    @Operation(summary = "Delete a field")
    public ResponseEntity<ApiResponse<Void>> deleteField(@PathVariable Long fieldId) {
        templateFieldService.deleteField(fieldId);
        return ResponseEntity.ok(ApiResponse.success("Field deleted successfully", null));
    }

    @PostMapping("/fields/reorder")
    @Operation(summary = "Reorder fields within a section")
    public ResponseEntity<ApiResponse<Void>> reorderFields(
            @Valid @RequestBody FieldReorderRequest request) {
        templateFieldService.reorderFields(request);
        return ResponseEntity.ok(ApiResponse.success("Fields reordered successfully", null));
    }
}
