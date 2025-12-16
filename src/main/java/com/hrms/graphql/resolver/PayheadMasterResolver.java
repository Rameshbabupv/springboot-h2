package com.hrms.graphql.resolver;

import com.hrms.entity.PayheadMaster;
import com.hrms.enums.PayheadType;
import com.hrms.graphql.input.PayheadInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.PayheadMasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Resolver for Payhead Master operations
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
public class PayheadMasterResolver {

    private final PayheadMasterService payheadMasterService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    // ==================== Queries ====================

    @QueryMapping
    public List<PayheadMaster> payheadMaster(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long companyId,
            @Argument PayheadType type,
            @Argument Boolean isActive) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.debug("GraphQL Query: payheadMaster - tenant: {}, company: {}, type: {}, active: {}",
                  tenantId, companyId, type, isActive);

        // Try tenant-specific payheads first
        List<PayheadMaster> payheads = payheadMasterService.getPayheads(tenantId, companyId, type, isActive);

        // Fallback to SYSTEM payheads if none found
        if ((payheads == null || payheads.isEmpty()) && !"SYSTEM".equals(tenantId)) {
            log.info("No payheads found for tenantId={}, falling back to SYSTEM payheads", tenantId);
            payheads = payheadMasterService.getPayheads("SYSTEM", null, type, isActive);
        }

        log.debug("GraphQL Response: payheadMaster - returned {} payheads",
                  payheads != null ? payheads.size() : 0);

        return payheads;
    }

    @QueryMapping
    public PayheadMaster payheadById(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.debug("GraphQL Query: payheadById - tenant: {}, id: {}", tenantId, id);

        return payheadMasterService.getPayheadById(tenantId, id);
    }

    // ==================== Mutations ====================

    @MutationMapping
    public PayheadMaster createPayhead(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long companyId,
            @Argument PayheadInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.info("GraphQL Mutation: createPayhead - tenant: {}, company: {}, code: {}",
                 tenantId, companyId, input.getPayheadCode());

        return payheadMasterService.createPayhead(tenantId, companyId, input);
    }

    @MutationMapping
    public PayheadMaster updatePayhead(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long id,
            @Argument PayheadInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.info("GraphQL Mutation: updatePayhead - tenant: {}, id: {}", tenantId, id);

        return payheadMasterService.updatePayhead(tenantId, id, input);
    }

    @MutationMapping
    public Boolean deletePayhead(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.info("GraphQL Mutation: deletePayhead - tenant: {}, id: {}", tenantId, id);

        return payheadMasterService.deletePayhead(tenantId, id);
    }
}
