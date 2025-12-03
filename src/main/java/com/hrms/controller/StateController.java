package com.hrms.controller;

import com.hrms.dto.request.StateRequest;
import com.hrms.dto.response.ApiResponse;
import com.hrms.entity.State;
import com.hrms.service.StateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for State operations.
 */
@RestController
@RequestMapping("/api/states")
@RequiredArgsConstructor
@Tag(name = "State", description = "State management APIs")
public class StateController {

    private final StateService stateService;

    @GetMapping
    @Operation(summary = "Get all states")
    public ResponseEntity<ApiResponse<List<State>>> getAllStates() {
        List<State> states = stateService.getAllStates();
        return ResponseEntity.ok(ApiResponse.success("States fetched successfully", states));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get state by ID")
    public ResponseEntity<ApiResponse<State>> getStateById(@PathVariable Long id) {
        State state = stateService.getStateById(id);
        return ResponseEntity.ok(ApiResponse.success("State fetched successfully", state));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get states by tenant ID")
    public ResponseEntity<ApiResponse<List<State>>> getStatesByTenant(@PathVariable String tenantId) {
        List<State> states = stateService.getStatesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("States fetched successfully", states));
    }

    @GetMapping("/tenant/{tenantId}/active")
    @Operation(summary = "Get active states by tenant ID")
    public ResponseEntity<ApiResponse<List<State>>> getActiveStatesByTenant(@PathVariable String tenantId) {
        List<State> states = stateService.getActiveStatesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Active states fetched successfully", states));
    }

    @GetMapping("/tenant/{tenantId}/search")
    @Operation(summary = "Search states by tenant ID")
    public ResponseEntity<ApiResponse<List<State>>> searchStates(
            @PathVariable String tenantId,
            @RequestParam(required = false) String searchTerm) {
        List<State> states = stateService.searchStates(tenantId, searchTerm);
        return ResponseEntity.ok(ApiResponse.success("States search completed successfully", states));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active states")
    public ResponseEntity<ApiResponse<List<State>>> getActiveStates() {
        List<State> states = stateService.getActiveStates();
        return ResponseEntity.ok(ApiResponse.success("Active states fetched successfully", states));
    }

    @GetMapping("/country/{countryId}")
    @Operation(summary = "Get states by country ID")
    public ResponseEntity<ApiResponse<List<State>>> getStatesByCountry(@PathVariable Long countryId) {
        List<State> states = stateService.getStatesByCountry(countryId);
        return ResponseEntity.ok(ApiResponse.success("States fetched successfully", states));
    }

    @GetMapping("/tenant/{tenantId}/country/{countryId}")
    @Operation(summary = "Get states by tenant ID and country ID")
    public ResponseEntity<ApiResponse<List<State>>> getStatesByTenantAndCountry(
            @PathVariable String tenantId,
            @PathVariable Long countryId) {
        List<State> states = stateService.getStatesByTenantAndCountry(tenantId, countryId);
        return ResponseEntity.ok(ApiResponse.success("States fetched successfully", states));
    }

    @PostMapping
    @Operation(summary = "Create a new state")
    public ResponseEntity<ApiResponse<State>> createState(@Valid @RequestBody StateRequest request) {
        State created = stateService.createState(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("State created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing state")
    public ResponseEntity<ApiResponse<State>> updateState(
            @PathVariable Long id,
            @Valid @RequestBody StateRequest request) {
        State updated = stateService.updateState(id, request);
        return ResponseEntity.ok(ApiResponse.success("State updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a state")
    public ResponseEntity<ApiResponse<Void>> deleteState(@PathVariable Long id) {
        stateService.deleteState(id);
        return ResponseEntity.ok(ApiResponse.success("State deleted successfully", null));
    }
}
