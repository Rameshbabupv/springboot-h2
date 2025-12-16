package com.hrms.graphql.resolver;

import com.hrms.dto.response.DeleteLeaveTypeResponse;
import com.hrms.entity.LeaveType;
import com.hrms.graphql.input.LeaveTypeInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Resolver for LeaveType operations.
 * Implements queries and mutations for leave type master data management.
 *
 * JWT Integration:
 * - tenantId is extracted from JWT token (preferred)
 * - @Argument tenantId kept for backward compatibility during migration
 * - JWT takes precedence when available
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class LeaveTypeResolver {

    private final LeaveTypeService leaveTypeService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    // ========== Queries ==========

    /**
     * Get all leave types for a company with optional isActive filter.
     * GraphQL Query: leaveTypes(tenantId: String!, companyId: ID!, isActive: Boolean): [LeaveType!]!
     * tenantId is extracted from JWT; argument kept for backward compatibility.
     */
    @QueryMapping
    public List<LeaveType> leaveTypes(@Argument(name = "tenantId") String tenantIdArg,
                                       @Argument Long companyId,
                                       @Argument Boolean isActive) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: leaveTypes - tenantId: {}, companyId: {}, isActive: {}",
                  tenantId, companyId, isActive);
        return leaveTypeService.getLeaveTypes(tenantId, companyId, isActive);
    }

    /**
     * Get single leave type by ID.
     * GraphQL Query: leaveType(tenantId: String!, companyId: ID!, id: ID!): LeaveType
     * tenantId is extracted from JWT; argument kept for backward compatibility.
     */
    @QueryMapping
    public LeaveType leaveType(@Argument(name = "tenantId") String tenantIdArg,
                                @Argument Long companyId,
                                @Argument Long id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Query: leaveType - tenantId: {}, companyId: {}, id: {}",
                  tenantId, companyId, id);
        return leaveTypeService.getLeaveTypeById(tenantId, companyId, id).orElse(null);
    }

    // ========== Mutations ==========

    /**
     * Create new leave type.
     * GraphQL Mutation: createLeaveType(tenantId: String!, companyId: ID!, input: LeaveTypeInput!): LeaveType!
     * tenantId is extracted from JWT; argument kept for backward compatibility.
     */
    @MutationMapping
    public LeaveType createLeaveType(@Argument(name = "tenantId") String tenantIdArg,
                                      @Argument Long companyId,
                                      @Argument LeaveTypeInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Mutation: createLeaveType - tenantId: {}, companyId: {}, code: {}",
                  tenantId, companyId, input.getCode());
        return leaveTypeService.createLeaveType(tenantId, companyId, input);
    }

    /**
     * Update existing leave type.
     * GraphQL Mutation: updateLeaveType(tenantId: String!, companyId: ID!, id: ID!, input: LeaveTypeInput!): LeaveType!
     * tenantId is extracted from JWT; argument kept for backward compatibility.
     */
    @MutationMapping
    public LeaveType updateLeaveType(@Argument(name = "tenantId") String tenantIdArg,
                                      @Argument Long companyId,
                                      @Argument Long id,
                                      @Argument LeaveTypeInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Mutation: updateLeaveType - tenantId: {}, companyId: {}, id: {}",
                  tenantId, companyId, id);
        return leaveTypeService.updateLeaveType(tenantId, companyId, id, input);
    }

    /**
     * Delete leave type (soft or hard delete based on references).
     * GraphQL Mutation: deleteLeaveType(tenantId: String!, companyId: ID!, id: ID!): DeleteLeaveTypeResponse!
     * tenantId is extracted from JWT; argument kept for backward compatibility.
     */
    @MutationMapping
    public DeleteLeaveTypeResponse deleteLeaveType(@Argument(name = "tenantId") String tenantIdArg,
                                                     @Argument Long companyId,
                                                     @Argument Long id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("GraphQL Mutation: deleteLeaveType - tenantId: {}, companyId: {}, id: {}",
                  tenantId, companyId, id);
        return leaveTypeService.deleteLeaveType(tenantId, companyId, id);
    }

    // ========== Field Resolvers ==========

    /**
     * Resolve companyId field from company entity.
     * Converts ManyToOne relationship to scalar ID for GraphQL response.
     */
    @SchemaMapping(typeName = "LeaveType", field = "companyId")
    public Long companyId(LeaveType leaveType) {
        return leaveType.getCompany() != null ? leaveType.getCompany().getId() : null;
    }
}
