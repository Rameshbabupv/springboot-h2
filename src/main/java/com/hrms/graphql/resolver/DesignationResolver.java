package com.hrms.graphql.resolver;

import com.hrms.dto.request.DesignationRequest;
import com.hrms.entity.Designation;
import com.hrms.graphql.input.DesignationInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.DesignationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DesignationResolver {

    private final DesignationService designationService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    @QueryMapping
    public List<Designation> designations() {
        return designationService.getAllDesignations();
    }

    @QueryMapping
    public Designation designation(@Argument Long id) {
        return designationService.getDesignationById(id);
    }

    @QueryMapping
    public List<Designation> designationsByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return designationService.getDesignationsByTenant(tenantId);
    }

    @QueryMapping
    public List<Designation> activeDesignationsByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return designationService.getActiveDesignationsByTenant(tenantId);
    }

    @QueryMapping
    public List<Designation> activeDesignations() {
        return designationService.getActiveDesignations();
    }

    @QueryMapping
    public List<Designation> searchDesignations(@Argument(name = "tenantId") String tenantIdArg, @Argument String searchTerm) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return designationService.searchDesignations(tenantId, searchTerm);
    }

    /**
     * Get designations for selection with organizational scope filtering.
     * Supports edit mode to include current value even if outside scope.
     */
    @QueryMapping
    public List<Designation> designationsForSelection(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument String userId,
            @Argument Boolean isEditMode,
            @Argument String currentDesignationId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        Long userIdLong = Long.parseLong(userId);
        Long currentIdLong = currentDesignationId != null ? Long.parseLong(currentDesignationId) : null;
        boolean editMode = isEditMode != null && isEditMode;

        log.debug("GraphQL Query: designationsForSelection - tenantId: {}, userId: {}, editMode: {}", tenantId, userId, editMode);
        List<Designation> result = designationService.getDesignationsForSelection(tenantId, userIdLong, editMode, currentIdLong);
        log.info("GraphQL Response: designationsForSelection - returned {} designations", result.size());
        return result;
    }

    @MutationMapping
    public Designation createDesignation(@Argument DesignationInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);

        DesignationRequest request = mapToRequest(input, tenantId);
        return designationService.createDesignation(request);
    }

    @MutationMapping
    public Designation updateDesignation(@Argument Long id, @Argument DesignationInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);

        DesignationRequest request = mapToRequest(input, tenantId);
        return designationService.updateDesignation(id, request);
    }

    @MutationMapping
    public Boolean deleteDesignation(@Argument Long id) {
        designationService.deleteDesignation(id);
        return true;
    }

    private DesignationRequest mapToRequest(DesignationInput input, String tenantId) {
        return DesignationRequest.builder()
                .tenantId(tenantId)
                .name(input.getName())
                .code(input.getCode())
                .description(input.getDescription())
                .isActive(input.getIsActive())
                .build();
    }
}
