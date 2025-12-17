package com.hrms.graphql.resolver;

import com.hrms.dto.request.GradeRequest;
import com.hrms.entity.Grade;
import com.hrms.graphql.input.GradeInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.GradeService;
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
public class GradeResolver {

    private final GradeService gradeService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    @QueryMapping
    public List<Grade> grades() {
        return gradeService.getAllGrades();
    }

    @QueryMapping
    public Grade grade(@Argument Long id) {
        return gradeService.getGradeById(id);
    }

    @QueryMapping
    public List<Grade> gradesByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return gradeService.getGradesByTenant(tenantId);
    }

    @QueryMapping
    public List<Grade> activeGrades() {
        return gradeService.getActiveGrades();
    }

    @QueryMapping
    public List<Grade> searchGrades(@Argument(name = "tenantId") String tenantIdArg, @Argument String searchTerm) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return gradeService.searchGrades(tenantId, searchTerm);
    }

    /**
     * Get grades for selection with organizational scope filtering.
     * Supports edit mode to include current value even if outside scope.
     */
    @QueryMapping
    public List<Grade> gradesForSelection(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument String userId,
            @Argument Boolean isEditMode,
            @Argument String currentGradeId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        Long userIdLong = Long.parseLong(userId);
        Long currentIdLong = currentGradeId != null ? Long.parseLong(currentGradeId) : null;
        boolean editMode = isEditMode != null && isEditMode;

        log.debug("GraphQL Query: gradesForSelection - tenantId: {}, userId: {}, editMode: {}", tenantId, userId, editMode);
        List<Grade> result = gradeService.getGradesForSelection(tenantId, userIdLong, editMode, currentIdLong);
        log.info("GraphQL Response: gradesForSelection - returned {} grades", result.size());
        return result;
    }

    @MutationMapping
    public Grade createGrade(@Argument GradeInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        GradeRequest request = mapToRequest(input, tenantId);
        return gradeService.createGrade(request);
    }

    @MutationMapping
    public Grade updateGrade(@Argument Long id, @Argument GradeInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        GradeRequest request = mapToRequest(input, tenantId);
        return gradeService.updateGrade(id, request);
    }

    @MutationMapping
    public Boolean deleteGrade(@Argument Long id) {
        gradeService.deleteGrade(id);
        return true;
    }

    private GradeRequest mapToRequest(GradeInput input, String tenantId) {
        Long userId = jwtClaimsExtractor.getUserIdOrNull();
        String userIdStr = userId != null ? userId.toString() : null;

        return GradeRequest.builder()
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
