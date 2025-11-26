package com.hrms.controller;

import com.hrms.dto.request.JobFunctionRequest;
import com.hrms.dto.response.ApiResponse;
import com.hrms.entity.JobFunction;
import com.hrms.service.JobFunctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for JobFunction operations.
 */
@RestController
@RequestMapping("/api/job-functions")
@RequiredArgsConstructor
@Tag(name = "JobFunction", description = "Job Function management APIs")
public class JobFunctionController {

    private final JobFunctionService jobFunctionService;

    @GetMapping
    @Operation(summary = "Get all job functions")
    public ResponseEntity<ApiResponse<List<JobFunction>>> getAllJobFunctions() {
        List<JobFunction> jobFunctions = jobFunctionService.getAllJobFunctions();
        return ResponseEntity.ok(ApiResponse.success("Job functions fetched successfully", jobFunctions));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job function by ID")
    public ResponseEntity<ApiResponse<JobFunction>> getJobFunctionById(@PathVariable Long id) {
        JobFunction jobFunction = jobFunctionService.getJobFunctionById(id);
        return ResponseEntity.ok(ApiResponse.success("Job function fetched successfully", jobFunction));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get job functions by tenant ID")
    public ResponseEntity<ApiResponse<List<JobFunction>>> getJobFunctionsByTenant(@PathVariable String tenantId) {
        List<JobFunction> jobFunctions = jobFunctionService.getJobFunctionsByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Job functions fetched successfully", jobFunctions));
    }

    @GetMapping("/tenant/{tenantId}/active")
    @Operation(summary = "Get active job functions by tenant ID")
    public ResponseEntity<ApiResponse<List<JobFunction>>> getActiveJobFunctionsByTenant(@PathVariable String tenantId) {
        List<JobFunction> jobFunctions = jobFunctionService.getActiveJobFunctionsByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Active job functions fetched successfully", jobFunctions));
    }

    @GetMapping("/tenant/{tenantId}/group/{functionGroup}")
    @Operation(summary = "Get job functions by tenant ID and function group")
    public ResponseEntity<ApiResponse<List<JobFunction>>> getJobFunctionsByGroup(
            @PathVariable String tenantId,
            @PathVariable String functionGroup) {
        List<JobFunction> jobFunctions = jobFunctionService.getJobFunctionsByGroup(tenantId, functionGroup);
        return ResponseEntity.ok(ApiResponse.success("Job functions fetched successfully", jobFunctions));
    }

    @GetMapping("/tenant/{tenantId}/search")
    @Operation(summary = "Search job functions by tenant ID")
    public ResponseEntity<ApiResponse<List<JobFunction>>> searchJobFunctions(
            @PathVariable String tenantId,
            @RequestParam(required = false) String searchTerm) {
        List<JobFunction> jobFunctions = jobFunctionService.searchJobFunctions(tenantId, searchTerm);
        return ResponseEntity.ok(ApiResponse.success("Job functions search completed successfully", jobFunctions));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active job functions")
    public ResponseEntity<ApiResponse<List<JobFunction>>> getActiveJobFunctions() {
        List<JobFunction> jobFunctions = jobFunctionService.getActiveJobFunctions();
        return ResponseEntity.ok(ApiResponse.success("Active job functions fetched successfully", jobFunctions));
    }

    @PostMapping
    @Operation(summary = "Create a new job function")
    public ResponseEntity<ApiResponse<JobFunction>> createJobFunction(@Valid @RequestBody JobFunctionRequest request) {
        JobFunction created = jobFunctionService.createJobFunction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job function created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing job function")
    public ResponseEntity<ApiResponse<JobFunction>> updateJobFunction(
            @PathVariable Long id,
            @Valid @RequestBody JobFunctionRequest request) {
        JobFunction updated = jobFunctionService.updateJobFunction(id, request);
        return ResponseEntity.ok(ApiResponse.success("Job function updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a job function")
    public ResponseEntity<ApiResponse<Void>> deleteJobFunction(@PathVariable Long id) {
        jobFunctionService.deleteJobFunction(id);
        return ResponseEntity.ok(ApiResponse.success("Job function deleted successfully", null));
    }
}
