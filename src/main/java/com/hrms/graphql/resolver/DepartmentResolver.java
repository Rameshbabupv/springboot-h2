package com.hrms.graphql.resolver;

import com.hrms.dto.request.DepartmentRequest;
import com.hrms.entity.Department;
import com.hrms.graphql.input.DepartmentInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Resolver for Department operations.
 *
 * JWT Integration:
 * - tenantId is extracted from JWT token (preferred)
 * - @Argument tenantId kept for backward compatibility during migration
 * - JWT takes precedence when available
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DepartmentResolver {

    private final DepartmentService departmentService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    @QueryMapping
    public List<Department> departments() {
        log.debug("GraphQL Query: departments");
        List<Department> result = departmentService.getAllDepartments();
        log.info("GraphQL Response: departments - returned {} departments", result.size());
        return result;
    }

    @QueryMapping
    public Department department(@Argument Long id) {
        log.debug("GraphQL Query: department - id: {}", id);
        Department result = departmentService.getDepartmentById(id);
        log.info("GraphQL Response: department - returned: {}", result.getName());
        return result;
    }

    /**
     * Get departments by tenant.
     * tenantId is extracted from JWT; argument kept for backward compatibility.
     */
    @QueryMapping
    public List<Department> departmentsByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: departmentsByTenant - tenantId: {}", tenantId);
        List<Department> result = departmentService.getDepartmentsByTenant(tenantId);
        log.info("GraphQL Response: departmentsByTenant - returned {} departments for tenant: {}", result.size(), tenantId);
        return result;
    }

    /**
     * Get active departments by tenant.
     * tenantId is extracted from JWT; argument kept for backward compatibility.
     */
    @QueryMapping
    public List<Department> activeDepartmentsByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: activeDepartmentsByTenant - tenantId: {}", tenantId);
        List<Department> result = departmentService.getActiveDepartmentsByTenant(tenantId);
        log.info("GraphQL Response: activeDepartmentsByTenant - returned {} active departments", result.size());
        return result;
    }

    @QueryMapping
    public List<Department> activeDepartments() {
        log.debug("GraphQL Query: activeDepartments");
        List<Department> result = departmentService.getActiveDepartments();
        log.info("GraphQL Response: activeDepartments - returned {} active departments", result.size());
        return result;
    }

    /**
     * Search departments.
     * tenantId is extracted from JWT; argument kept for backward compatibility.
     */
    @QueryMapping
    public List<Department> searchDepartments(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument String searchTerm) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: searchDepartments - tenantId: {}, searchTerm: {}", tenantId, searchTerm);
        List<Department> result = departmentService.searchDepartments(tenantId, searchTerm);
        log.info("GraphQL Response: searchDepartments - returned {} departments matching: {}", result.size(), searchTerm);
        return result;
    }

    /**
     * Get departments for selection with organizational scope filtering.
     * Supports edit mode to include current value even if outside scope.
     */
    @QueryMapping
    public List<Department> departmentsForSelection(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument String userId,
            @Argument Boolean isEditMode,
            @Argument String currentDepartmentId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        Long userIdLong = Long.parseLong(userId);
        Long currentIdLong = currentDepartmentId != null ? Long.parseLong(currentDepartmentId) : null;
        boolean editMode = isEditMode != null && isEditMode;

        log.debug("GraphQL Query: departmentsForSelection - tenantId: {}, userId: {}, editMode: {}", tenantId, userId, editMode);
        List<Department> result = departmentService.getDepartmentsForSelection(tenantId, userIdLong, editMode, currentIdLong);
        log.info("GraphQL Response: departmentsForSelection - returned {} departments", result.size());
        return result;
    }

    @MutationMapping
    public Department createDepartment(@Argument DepartmentInput input) {
        // Get tenantId from JWT if not provided in input
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);

        log.debug("GraphQL Mutation: createDepartment - name: {}, code: {}, tenantId: {}",
                  input.getName(), input.getCode(), tenantId);

        DepartmentRequest request = mapToRequest(input, tenantId);
        Department result = departmentService.createDepartment(request);
        log.info("GraphQL Response: createDepartment - created department id: {}, name: {}", result.getId(), result.getName());
        return result;
    }

    @MutationMapping
    public Department updateDepartment(@Argument Long id, @Argument DepartmentInput input) {
        log.debug("GraphQL Mutation: updateDepartment - id: {}", id);

        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);

        DepartmentRequest request = mapToRequest(input, tenantId);
        Department result = departmentService.updateDepartment(id, request);
        log.info("GraphQL Response: updateDepartment - updated department id: {}, name: {}", result.getId(), result.getName());
        return result;
    }

    @MutationMapping
    public Boolean deleteDepartment(@Argument Long id) {
        log.debug("GraphQL Mutation: deleteDepartment - id: {}", id);
        departmentService.deleteDepartment(id);
        log.info("GraphQL Response: deleteDepartment - successfully deleted department id: {}", id);
        return true;
    }

    private DepartmentRequest mapToRequest(DepartmentInput input, String tenantId) {
        Long userId = jwtClaimsExtractor.getUserIdOrNull();
        String userIdStr = userId != null ? userId.toString() : null;

        return DepartmentRequest.builder()
                .tenantId(tenantId)
                .name(input.getName())
                .code(input.getCode())
                .description(input.getDescription())
                .isActive(input.getIsActive())
                .createdBy(input.getCreatedBy() != null ? input.getCreatedBy() : userIdStr)
                .updatedBy(input.getUpdatedBy() != null ? input.getUpdatedBy() : userIdStr)
                .build();
    }
}
