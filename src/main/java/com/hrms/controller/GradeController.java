package com.hrms.controller;

import com.hrms.dto.request.GradeRequest;
import com.hrms.dto.response.ApiResponse;
import com.hrms.entity.Grade;
import com.hrms.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Grade operations.
 */
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@Tag(name = "Grade", description = "Grade management APIs")
public class GradeController {

    private final GradeService gradeService;

    @GetMapping
    @Operation(summary = "Get all grades")
    public ResponseEntity<ApiResponse<List<Grade>>> getAllGrades() {
        List<Grade> grades = gradeService.getAllGrades();
        return ResponseEntity.ok(ApiResponse.success("Grades fetched successfully", grades));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get grade by ID")
    public ResponseEntity<ApiResponse<Grade>> getGradeById(@PathVariable Long id) {
        Grade grade = gradeService.getGradeById(id);
        return ResponseEntity.ok(ApiResponse.success("Grade fetched successfully", grade));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get grades by tenant ID")
    public ResponseEntity<ApiResponse<List<Grade>>> getGradesByTenant(@PathVariable String tenantId) {
        List<Grade> grades = gradeService.getGradesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Grades fetched successfully", grades));
    }

    @GetMapping("/tenant/{tenantId}/active")
    @Operation(summary = "Get active grades by tenant ID")
    public ResponseEntity<ApiResponse<List<Grade>>> getActiveGradesByTenant(@PathVariable String tenantId) {
        List<Grade> grades = gradeService.getActiveGradesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Active grades fetched successfully", grades));
    }

    @GetMapping("/tenant/{tenantId}/search")
    @Operation(summary = "Search grades by tenant ID")
    public ResponseEntity<ApiResponse<List<Grade>>> searchGrades(
            @PathVariable String tenantId,
            @RequestParam(required = false) String searchTerm) {
        List<Grade> grades = gradeService.searchGrades(tenantId, searchTerm);
        return ResponseEntity.ok(ApiResponse.success("Grades search completed successfully", grades));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active grades")
    public ResponseEntity<ApiResponse<List<Grade>>> getActiveGrades() {
        List<Grade> grades = gradeService.getActiveGrades();
        return ResponseEntity.ok(ApiResponse.success("Active grades fetched successfully", grades));
    }

    @PostMapping
    @Operation(summary = "Create a new grade")
    public ResponseEntity<ApiResponse<Grade>> createGrade(@Valid @RequestBody GradeRequest request) {
        Grade created = gradeService.createGrade(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Grade created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing grade")
    public ResponseEntity<ApiResponse<Grade>> updateGrade(
            @PathVariable Long id,
            @Valid @RequestBody GradeRequest request) {
        Grade updated = gradeService.updateGrade(id, request);
        return ResponseEntity.ok(ApiResponse.success("Grade updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a grade")
    public ResponseEntity<ApiResponse<Void>> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.ok(ApiResponse.success("Grade deleted successfully", null));
    }
}
