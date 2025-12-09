package com.hrms.graphql.resolver;

import com.hrms.entity.AttendancePolicyTemplate;
import com.hrms.graphql.input.AttendancePolicyTemplateInput;
import com.hrms.service.AttendancePolicyTemplateService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL resolver for AttendancePolicyTemplate operations.
 */
@Controller
public class AttendancePolicyTemplateResolver {

    private final AttendancePolicyTemplateService templateService;

    public AttendancePolicyTemplateResolver(AttendancePolicyTemplateService templateService) {
        this.templateService = templateService;
    }

    // Queries

    @QueryMapping
    public List<AttendancePolicyTemplate> attendancePolicyTemplates(
            @Argument String tenantId,
            @Argument Long companyId,
            @Argument Boolean isActive,
            @Argument String searchQuery) {
        return templateService.getTemplates(tenantId, companyId, isActive, searchQuery);
    }

    @QueryMapping
    public AttendancePolicyTemplate attendancePolicyTemplate(
            @Argument String tenantId,
            @Argument Long id) {
        return templateService.getTemplate(tenantId, id).orElse(null);
    }

    // Mutations

    @MutationMapping
    public AttendancePolicyTemplate createAttendancePolicyTemplate(
            @Argument String tenantId,
            @Argument Long companyId,
            @Argument AttendancePolicyTemplateInput input) {
        return templateService.createTemplate(tenantId, companyId, input);
    }

    @MutationMapping
    public AttendancePolicyTemplate updateAttendancePolicyTemplate(
            @Argument String tenantId,
            @Argument Long id,
            @Argument AttendancePolicyTemplateInput input) {
        return templateService.updateTemplate(tenantId, id, input);
    }

    @MutationMapping
    public Boolean deleteAttendancePolicyTemplate(
            @Argument String tenantId,
            @Argument Long id) {
        return templateService.deleteTemplate(tenantId, id);
    }

    @MutationMapping
    public AttendancePolicyTemplate setDefaultAttendancePolicyTemplate(
            @Argument String tenantId,
            @Argument Long id) {
        return templateService.setDefaultTemplate(tenantId, id);
    }
}
