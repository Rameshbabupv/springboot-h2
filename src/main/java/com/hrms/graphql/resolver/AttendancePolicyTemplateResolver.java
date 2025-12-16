package com.hrms.graphql.resolver;

import com.hrms.entity.AttendancePolicyTemplate;
import com.hrms.entity.Shift;
import com.hrms.graphql.input.AttendancePolicyTemplateInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.AttendancePolicyTemplateService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GraphQL resolver for AttendancePolicyTemplate operations.
 */
@Controller
public class AttendancePolicyTemplateResolver {

    private final AttendancePolicyTemplateService templateService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    public AttendancePolicyTemplateResolver(AttendancePolicyTemplateService templateService,
                                           JwtClaimsExtractor jwtClaimsExtractor) {
        this.templateService = templateService;
        this.jwtClaimsExtractor = jwtClaimsExtractor;
    }

    // Schema Mappings for field name differences

    @SchemaMapping(typeName = "AttendancePolicyTemplate", field = "isOTEligible")
    public Boolean isOTEligible(AttendancePolicyTemplate template) {
        return template.getIsOtEligible();
    }

    @SchemaMapping(typeName = "AttendancePolicyTemplate", field = "applicableShifts")
    public List<Shift> applicableShifts(AttendancePolicyTemplate template) {
        // Extract Shift objects from PolicyShift join entities
        return template.getPolicyShifts().stream()
                .map(policyShift -> policyShift.getShift())
                .collect(Collectors.toList());
    }

    // Queries

    @QueryMapping
    public List<AttendancePolicyTemplate> attendancePolicyTemplates(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long companyId,
            @Argument Boolean isActive,
            @Argument String searchQuery) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return templateService.getTemplates(tenantId, companyId, isActive, searchQuery);
    }

    @QueryMapping
    public AttendancePolicyTemplate attendancePolicyTemplate(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return templateService.getTemplate(tenantId, id).orElse(null);
    }

    // Mutations

    @MutationMapping
    public AttendancePolicyTemplate createAttendancePolicyTemplate(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long companyId,
            @Argument AttendancePolicyTemplateInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return templateService.createTemplate(tenantId, companyId, input);
    }

    @MutationMapping
    public AttendancePolicyTemplate updateAttendancePolicyTemplate(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long id,
            @Argument AttendancePolicyTemplateInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return templateService.updateTemplate(tenantId, id, input);
    }

    @MutationMapping
    public Boolean deleteAttendancePolicyTemplate(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return templateService.deleteTemplate(tenantId, id);
    }

    @MutationMapping
    public AttendancePolicyTemplate setDefaultAttendancePolicyTemplate(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return templateService.setDefaultTemplate(tenantId, id);
    }
}
