package com.hrms.controller;

import com.hrms.dto.request.EmployeeRequest;
import com.hrms.dto.response.ApiResponse;
import com.hrms.entity.Employee;
import com.hrms.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Employee operations.
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee management APIs")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @Operation(summary = "Get all employees")
    public ResponseEntity<ApiResponse<List<Employee>>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(ApiResponse.success("Employees fetched successfully", employees));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<Employee>> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee fetched successfully", employee));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get employees by tenant ID")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByTenant(@PathVariable String tenantId) {
        List<Employee> employees = employeeService.getEmployeesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Employees fetched successfully", employees));
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get employees by company ID")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByCompany(@PathVariable Long companyId) {
        List<Employee> employees = employeeService.getEmployeesByCompany(companyId);
        return ResponseEntity.ok(ApiResponse.success("Employees fetched successfully", employees));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get employees by department ID")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByDepartment(@PathVariable Long departmentId) {
        List<Employee> employees = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Employees fetched successfully", employees));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active employees")
    public ResponseEntity<ApiResponse<List<Employee>>> getActiveEmployees() {
        List<Employee> employees = employeeService.getActiveEmployees();
        return ResponseEntity.ok(ApiResponse.success("Active employees fetched successfully", employees));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get employees by status")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByStatus(@PathVariable String status) {
        List<Employee> employees = employeeService.getEmployeesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Employees fetched successfully", employees));
    }

    @PostMapping
    @Operation(summary = "Create a new employee")
    public ResponseEntity<ApiResponse<Employee>> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        Employee created = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing employee")
    public ResponseEntity<ApiResponse<Employee>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        Employee updated = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully", null));
    }
}
