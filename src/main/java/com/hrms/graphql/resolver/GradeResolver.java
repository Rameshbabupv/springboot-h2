package com.hrms.graphql.resolver;

import com.hrms.dto.request.GradeRequest;
import com.hrms.entity.Grade;
import com.hrms.graphql.input.GradeInput;
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

    @QueryMapping
    public List<Grade> grades() {
        return gradeService.getAllGrades();
    }

    @QueryMapping
    public Grade grade(@Argument Long id) {
        return gradeService.getGradeById(id);
    }

    @QueryMapping
    public List<Grade> gradesByTenant(@Argument String tenantId) {
        return gradeService.getGradesByTenant(tenantId);
    }

    @QueryMapping
    public List<Grade> activeGrades() {
        return gradeService.getActiveGrades();
    }

    @QueryMapping
    public List<Grade> searchGrades(@Argument String tenantId, @Argument String searchTerm) {
        return gradeService.searchGrades(tenantId, searchTerm);
    }

    @MutationMapping
    public Grade createGrade(@Argument GradeInput input) {
        GradeRequest request = mapToRequest(input);
        return gradeService.createGrade(request);
    }

    @MutationMapping
    public Grade updateGrade(@Argument Long id, @Argument GradeInput input) {
        GradeRequest request = mapToRequest(input);
        return gradeService.updateGrade(id, request);
    }

    @MutationMapping
    public Boolean deleteGrade(@Argument Long id) {
        gradeService.deleteGrade(id);
        return true;
    }

    private GradeRequest mapToRequest(GradeInput input) {
        return GradeRequest.builder()
                .tenantId(input.getTenantId())
                .name(input.getName())
                .code(input.getCode())
                .description(input.getDescription())
                .isActive(input.getIsActive())
                .createdBy(input.getCreatedBy())
                .updatedBy(input.getUpdatedBy())
                .build();
    }
}
