package com.hrms.graphql.resolver;

import com.hrms.dto.request.RouteRequest;
import com.hrms.entity.Route;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.RouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL resolver for Route queries and mutations.
 *
 * JWT Integration:
 * - tenantId is extracted from JWT token (preferred)
 * - @Argument tenantId kept for backward compatibility during migration
 * - JWT takes precedence when available
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class RouteResolver {

    private final RouteService routeService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    /**
     * Query: Get all routes for a tenant.
     */
    @QueryMapping
    public List<Route> routesByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.info("GraphQL Query: routesByTenant for tenant: {}", tenantId);
        return routeService.getRoutesByTenant(tenantId);
    }

    /**
     * Query: Get route by ID.
     */
    @QueryMapping
    public Route routeById(@Argument Long id, @Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.info("GraphQL Query: routeById with id: {} for tenant: {}", id, tenantId);
        return routeService.getRouteById(id, tenantId);
    }

    /**
     * Mutation: Create a new route.
     */
    @MutationMapping
    public Route createRoute(@Argument RouteRequest input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        input.setTenantId(tenantId);
        log.info("GraphQL Mutation: createRoute with name: {}", input.getName());
        return routeService.createRoute(input);
    }

    /**
     * Mutation: Update an existing route.
     */
    @MutationMapping
    public Route updateRoute(@Argument Long id, @Argument RouteRequest input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        input.setTenantId(tenantId);
        log.info("GraphQL Mutation: updateRoute with id: {}", id);
        return routeService.updateRoute(id, input);
    }

    /**
     * Mutation: Delete a route.
     */
    @MutationMapping
    public Boolean deleteRoute(@Argument Long id, @Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.info("GraphQL Mutation: deleteRoute with id: {}", id);
        return routeService.deleteRoute(id, tenantId);
    }
}
