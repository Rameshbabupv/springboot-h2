package com.hrms.graphql.resolver;

import com.hrms.entity.SalaryTemplate;
import com.hrms.graphql.input.SalaryTemplateInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.SalaryTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Resolver for Salary Template operations
 *
 * JWT Integration:
 * - tenantId is extracted from JWT token (preferred)
 * - @Argument tenantId kept for backward compatibility during migration
 * - JWT takes precedence when available
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class SalaryTemplateResolver {

    private final SalaryTemplateService salaryTemplateService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    // ==================== Queries ====================

    @QueryMapping
    public List<SalaryTemplate> salaryTemplates(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long companyId,
            @Argument Long gradeId,
            @Argument Long designationId,
            @Argument Boolean isActive) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.debug("GraphQL Query: salaryTemplates - tenant: {}, company: {}, grade: {}, designation: {}, active: {}",
                  tenantId, companyId, gradeId, designationId, isActive);

        return salaryTemplateService.getSalaryTemplates(tenantId, companyId, gradeId, designationId, isActive);
    }

    @QueryMapping
    public SalaryTemplate salaryTemplateById(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.debug("GraphQL Query: salaryTemplateById - tenant: {}, id: {}", tenantId, id);

        return salaryTemplateService.getSalaryTemplateById(tenantId, id);
    }

    // ==================== Mutations ====================

    @MutationMapping
    public SalaryTemplate createSalaryTemplate(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long companyId,
            @Argument SalaryTemplateInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.info("GraphQL Mutation: createSalaryTemplate - tenant: {}, company: {}, code: {}",
                 tenantId, companyId, input.getTemplateCode());

        return salaryTemplateService.createSalaryTemplate(tenantId, companyId, input);
    }

    @MutationMapping
    public SalaryTemplate updateSalaryTemplate(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long id,
            @Argument SalaryTemplateInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.info("GraphQL Mutation: updateSalaryTemplate - tenant: {}, id: {}", tenantId, id);

        return salaryTemplateService.updateSalaryTemplate(tenantId, id, input);
    }

    @MutationMapping
    public Boolean deleteSalaryTemplate(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.info("GraphQL Mutation: deleteSalaryTemplate - tenant: {}, id: {}", tenantId, id);

        return salaryTemplateService.deleteSalaryTemplate(tenantId, id);
    }
}
