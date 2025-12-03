package com.hrms.controller;

import com.hrms.dto.request.EmployeeTemplateSectionRequest;
import com.hrms.dto.request.SectionReorderRequest;
import com.hrms.dto.response.ApiResponse;
import com.hrms.dto.response.EmployeeTemplateSectionResponse;
import com.hrms.service.EmployeeTemplateSectionService;
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
@Tag(name = "Employee Template Section", description = "Template Section Management APIs")
public class EmployeeTemplateSectionController {

    private final EmployeeTemplateSectionService sectionService;

    @GetMapping("/{templateId}/sections")
    @Operation(summary = "Get all sections for a template")
    public ResponseEntity<ApiResponse<List<EmployeeTemplateSectionResponse>>> getSectionsByTemplateId(
            @PathVariable Long templateId) {
        List<EmployeeTemplateSectionResponse> sections = sectionService.getSectionsByTemplateId(templateId);
        return ResponseEntity.ok(ApiResponse.success("Sections fetched successfully", sections));
    }

    @GetMapping("/sections/{sectionId}")
    @Operation(summary = "Get section by ID")
    public ResponseEntity<ApiResponse<EmployeeTemplateSectionResponse>> getSectionById(
            @PathVariable Long sectionId) {
        EmployeeTemplateSectionResponse section = sectionService.getSectionById(sectionId);
        return ResponseEntity.ok(ApiResponse.success("Section fetched successfully", section));
    }

    @PostMapping("/{templateId}/sections")
    @Operation(summary = "Add a new section to a template")
    public ResponseEntity<ApiResponse<EmployeeTemplateSectionResponse>> createSection(
            @PathVariable Long templateId,
            @Valid @RequestBody EmployeeTemplateSectionRequest request) {
        request.setTemplateId(templateId);
        EmployeeTemplateSectionResponse created = sectionService.createSection(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Section created successfully", created));
    }

    @PutMapping("/sections/{sectionId}")
    @Operation(summary = "Update a section")
    public ResponseEntity<ApiResponse<EmployeeTemplateSectionResponse>> updateSection(
            @PathVariable Long sectionId,
            @Valid @RequestBody EmployeeTemplateSectionRequest request) {
        EmployeeTemplateSectionResponse updated = sectionService.updateSection(sectionId, request);
        return ResponseEntity.ok(ApiResponse.success("Section updated successfully", updated));
    }

    @DeleteMapping("/sections/{sectionId}")
    @Operation(summary = "Delete a section")
    public ResponseEntity<ApiResponse<Void>> deleteSection(@PathVariable Long sectionId) {
        sectionService.deleteSection(sectionId);
        return ResponseEntity.ok(ApiResponse.success("Section deleted successfully", null));
    }

    @PostMapping("/sections/reorder")
    @Operation(summary = "Reorder sections")
    public ResponseEntity<ApiResponse<Void>> reorderSections(
            @Valid @RequestBody SectionReorderRequest request) {
        sectionService.reorderSections(request);
        return ResponseEntity.ok(ApiResponse.success("Sections reordered successfully", null));
    }
}
