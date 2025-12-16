package com.hrms.service;

import com.hrms.dto.request.RouteRequest;
import com.hrms.entity.Route;
import com.hrms.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for Route management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {

    private final RouteRepository routeRepository;

    /**
     * Get all active routes for a tenant.
     */
    public List<Route> getRoutesByTenant(String tenantId) {
        log.info("Fetching routes for tenant: {}", tenantId);
        return routeRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    /**
     * Get route by ID and tenant.
     */
    public Route getRouteById(Long id, String tenantId) {
        log.info("Fetching route with id: {} for tenant: {}", id, tenantId);
        return routeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Route not found"));
    }

    /**
     * Create a new route.
     */
    @Transactional
    public Route createRoute(RouteRequest request) {
        log.info("Creating route: {} for tenant: {}", request.getName(), request.getTenantId());

        // Check for duplicate name
        if (routeRepository.findByTenantIdAndName(request.getTenantId(), request.getName()).isPresent()) {
            throw new RuntimeException("Route with name '" + request.getName() + "' already exists");
        }

        // Check for duplicate code
        if (routeRepository.findByTenantIdAndCode(request.getTenantId(), request.getCode()).isPresent()) {
            throw new RuntimeException("Route with code '" + request.getCode() + "' already exists");
        }

        Route route = new Route();
        route.setTenantId(request.getTenantId());
        route.setName(request.getName());
        route.setCode(request.getCode().toUpperCase());
        route.setDescription(request.getDescription());
        route.setIsActive(true);
        route.setCreatedBy(request.getCreatedBy());
        route.setUpdatedBy(request.getUpdatedBy());

        return routeRepository.save(route);
    }

    /**
     * Update an existing route.
     */
    @Transactional
    public Route updateRoute(Long id, RouteRequest request) {
        log.info("Updating route with id: {} for tenant: {}", id, request.getTenantId());

        Route route = routeRepository.findByIdAndTenantId(id, request.getTenantId())
                .orElseThrow(() -> new RuntimeException("Route not found"));

        // Check for duplicate name (excluding current route)
        if (routeRepository.existsByTenantIdAndNameAndIdNot(request.getTenantId(), request.getName(), id)) {
            throw new RuntimeException("Route with name '" + request.getName() + "' already exists");
        }

        // Check for duplicate code (excluding current route)
        if (routeRepository.existsByTenantIdAndCodeAndIdNot(request.getTenantId(), request.getCode(), id)) {
            throw new RuntimeException("Route with code '" + request.getCode() + "' already exists");
        }

        route.setName(request.getName());
        route.setCode(request.getCode().toUpperCase());
        route.setDescription(request.getDescription());
        route.setUpdatedBy(request.getUpdatedBy());

        return routeRepository.save(route);
    }

    /**
     * Delete a route (soft delete).
     */
    @Transactional
    public boolean deleteRoute(Long id, String tenantId) {
        log.info("Deleting route with id: {} for tenant: {}", id, tenantId);

        Route route = routeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        route.setIsActive(false);
        routeRepository.save(route);

        return true;
    }

    /**
     * Hard delete a route (physical deletion).
     */
    @Transactional
    public boolean hardDeleteRoute(Long id, String tenantId) {
        log.info("Hard deleting route with id: {} for tenant: {}", id, tenantId);

        Route route = routeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        routeRepository.delete(route);

        return true;
    }
}
