package com.hrms.graphql.resolver;

import com.hrms.dto.request.EmploymentTypeRequest;
import com.hrms.entity.EmploymentType;
import com.hrms.graphql.input.EmploymentTypeInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.EmploymentTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Resolver for EmploymentType operations.
 *
 * JWT Integration:
 * - tenantId is extracted from JWT token (preferred)
 * - @Argument tenantId kept for backward compatibility during migration
 * - JWT takes precedence when available
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class EmploymentTypeResolver {

    private final EmploymentTypeService employmentTypeService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    @QueryMapping
    public List<EmploymentType> employmentTypes() {
        return employmentTypeService.getAllEmploymentTypes();
    }

    @QueryMapping
    public EmploymentType employmentType(@Argument Long id) {
        return employmentTypeService.getEmploymentTypeById(id);
    }

    @QueryMapping
    public List<EmploymentType> employmentTypesByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return employmentTypeService.getEmploymentTypesByTenant(tenantId);
    }

    @QueryMapping
    public List<EmploymentType> activeEmploymentTypes() {
        return employmentTypeService.getActiveEmploymentTypes();
    }

    @QueryMapping
    public List<EmploymentType> searchEmploymentTypes(@Argument(name = "tenantId") String tenantIdArg, @Argument String searchTerm) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return employmentTypeService.searchEmploymentTypes(tenantId, searchTerm);
    }

    /**
     * Get employment types for selection with organizational scope filtering.
     * Supports edit mode to include current value even if outside scope.
     */
    @QueryMapping
    public List<EmploymentType> employmentTypesForSelection(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument String userId,
            @Argument Boolean isEditMode,
            @Argument String currentEmploymentTypeId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        Long userIdLong = Long.parseLong(userId);
        Long currentIdLong = currentEmploymentTypeId != null ? Long.parseLong(currentEmploymentTypeId) : null;
        boolean editMode = isEditMode != null && isEditMode;

        log.debug("GraphQL Query: employmentTypesForSelection - tenantId: {}, userId: {}, editMode: {}", tenantId, userId, editMode);
        List<EmploymentType> result = employmentTypeService.getEmploymentTypesForSelection(tenantId, userIdLong, editMode, currentIdLong);
        log.info("GraphQL Response: employmentTypesForSelection - returned {} employment types", result.size());
        return result;
    }

    @MutationMapping
    public EmploymentType createEmploymentType(@Argument EmploymentTypeInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        EmploymentTypeRequest request = mapToRequest(input, tenantId);
        return employmentTypeService.createEmploymentType(request);
    }

    @MutationMapping
    public EmploymentType updateEmploymentType(@Argument Long id, @Argument EmploymentTypeInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        EmploymentTypeRequest request = mapToRequest(input, tenantId);
        return employmentTypeService.updateEmploymentType(id, request);
    }

    @MutationMapping
    public Boolean deleteEmploymentType(@Argument Long id) {
        employmentTypeService.deleteEmploymentType(id);
        return true;
    }

    private EmploymentTypeRequest mapToRequest(EmploymentTypeInput input, String tenantId) {
        return EmploymentTypeRequest.builder()
                .tenantId(tenantId)
                .name(input.getName())
                .code(input.getCode())
                .description(input.getDescription())
                .isActive(input.getIsActive())
                .build();
    }
}
