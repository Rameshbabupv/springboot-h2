package com.hrms.graphql.resolver;

import com.hrms.dto.request.JobFunctionRequest;
import com.hrms.entity.JobFunction;
import com.hrms.graphql.input.JobFunctionInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.JobFunctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Resolver for JobFunction operations.
 *
 * JWT Integration:
 * - tenantId is extracted from JWT token (preferred)
 * - @Argument tenantId kept for backward compatibility during migration
 * - JWT takes precedence when available
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class JobFunctionResolver {

    private final JobFunctionService jobFunctionService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    @QueryMapping
    public List<JobFunction> jobFunctions() {
        return jobFunctionService.getAllJobFunctions();
    }

    @QueryMapping
    public JobFunction jobFunction(@Argument Long id) {
        return jobFunctionService.getJobFunctionById(id);
    }

    @QueryMapping
    public List<JobFunction> jobFunctionsByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return jobFunctionService.getJobFunctionsByTenant(tenantId);
    }

    @QueryMapping
    public List<JobFunction> activeJobFunctionsByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return jobFunctionService.getActiveJobFunctionsByTenant(tenantId);
    }

    @QueryMapping
    public List<JobFunction> activeJobFunctions() {
        return jobFunctionService.getActiveJobFunctions();
    }

    @QueryMapping
    public List<JobFunction> jobFunctionsByGroup(@Argument(name = "tenantId") String tenantIdArg, @Argument String functionGroup) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return jobFunctionService.getJobFunctionsByGroup(tenantId, functionGroup);
    }

    @QueryMapping
    public List<JobFunction> searchJobFunctions(@Argument(name = "tenantId") String tenantIdArg, @Argument String searchTerm) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return jobFunctionService.searchJobFunctions(tenantId, searchTerm);
    }

    /**
     * Get job functions for selection with organizational scope filtering.
     * Supports edit mode to include current value even if outside scope.
     */
    @QueryMapping
    public List<JobFunction> jobFunctionsForSelection(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument String userId,
            @Argument Boolean isEditMode,
            @Argument String currentJobFunctionId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        Long userIdLong = Long.parseLong(userId);
        Long currentIdLong = currentJobFunctionId != null ? Long.parseLong(currentJobFunctionId) : null;
        boolean editMode = isEditMode != null && isEditMode;

        log.debug("GraphQL Query: jobFunctionsForSelection - tenantId: {}, userId: {}, editMode: {}", tenantId, userId, editMode);
        List<JobFunction> result = jobFunctionService.getJobFunctionsForSelection(tenantId, userIdLong, editMode, currentIdLong);
        log.info("GraphQL Response: jobFunctionsForSelection - returned {} job functions", result.size());
        return result;
    }

    @MutationMapping
    public JobFunction createJobFunction(@Argument JobFunctionInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        JobFunctionRequest request = mapToRequest(input, tenantId);
        return jobFunctionService.createJobFunction(request);
    }

    @MutationMapping
    public JobFunction updateJobFunction(@Argument Long id, @Argument JobFunctionInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        JobFunctionRequest request = mapToRequest(input, tenantId);
        return jobFunctionService.updateJobFunction(id, request);
    }

    @MutationMapping
    public Boolean deleteJobFunction(@Argument Long id) {
        jobFunctionService.deleteJobFunction(id);
        return true;
    }

    private JobFunctionRequest mapToRequest(JobFunctionInput input, String tenantId) {
        return JobFunctionRequest.builder()
                .tenantId(tenantId)
                .name(input.getName())
                .code(input.getCode())
                .description(input.getDescription())
                .functionGroup(input.getFunctionGroup())
                .isActive(input.getIsActive())
                .build();
    }
}
