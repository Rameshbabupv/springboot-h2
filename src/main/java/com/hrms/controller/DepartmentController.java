package com.hrms.controller;

import com.hrms.dto.request.DepartmentRequest;
import com.hrms.dto.response.ApiResponse;
import com.hrms.entity.Department;
import com.hrms.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Department operations.
 */
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Department", description = "Department management APIs")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @Operation(summary = "Get all departments")
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success("Departments fetched successfully", departments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID")
    public ResponseEntity<ApiResponse<Department>> getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Department fetched successfully", department));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get departments by tenant ID")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartmentsByTenant(@PathVariable String tenantId) {
        List<Department> departments = departmentService.getDepartmentsByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Departments fetched successfully", departments));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active departments")
    public ResponseEntity<ApiResponse<List<Department>>> getActiveDepartments() {
        List<Department> departments = departmentService.getActiveDepartments();
        return ResponseEntity.ok(ApiResponse.success("Active departments fetched successfully", departments));
    }

    @GetMapping("/tenant/{tenantId}/active")
    @Operation(summary = "Get active departments by tenant ID")
    public ResponseEntity<ApiResponse<List<Department>>> getActiveDepartmentsByTenant(@PathVariable String tenantId) {
        List<Department> departments = departmentService.getActiveDepartmentsByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Active departments fetched successfully", departments));
    }

    @GetMapping("/tenant/{tenantId}/search")
    @Operation(summary = "Search departments by name, code, or description")
    public ResponseEntity<ApiResponse<List<Department>>> searchDepartments(
            @PathVariable String tenantId,
            @RequestParam(required = false) String searchTerm) {
        List<Department> departments = departmentService.searchDepartments(tenantId, searchTerm);
        return ResponseEntity.ok(ApiResponse.success("Departments search completed successfully", departments));
    }

    @PostMapping
    @Operation(summary = "Create a new department")
    public ResponseEntity<ApiResponse<Department>> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        Department created = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing department")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        Department updated = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a department")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully", null));
    }
}
