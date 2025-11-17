package com.hrms.graphql.resolver;

import com.hrms.entity.Grade;
import com.hrms.graphql.input.GradeInput;
import com.hrms.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GradeResolver {

    private final GradeRepository gradeRepository;

    @QueryMapping
    public List<Grade> grades() {
        return gradeRepository.findAll();
    }

    @QueryMapping
    public Grade grade(@Argument Long id) {
        return gradeRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Grade> gradesByTenant(@Argument String tenantId) {
        return gradeRepository.findByTenantId(tenantId);
    }

    @QueryMapping
    public List<Grade> activeGrades() {
        return gradeRepository.findByIsActiveTrue();
    }

    @MutationMapping
    public Grade createGrade(@Argument GradeInput input) {
        Grade grade = mapToEntity(input);
        return gradeRepository.save(grade);
    }

    @MutationMapping
    public Grade updateGrade(@Argument Long id, @Argument GradeInput input) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grade not found"));

        updateEntityFromInput(grade, input);
        return gradeRepository.save(grade);
    }

    @MutationMapping
    public Boolean deleteGrade(@Argument Long id) {
        if (gradeRepository.existsById(id)) {
            gradeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Grade mapToEntity(GradeInput input) {
        Grade grade = new Grade();
        updateEntityFromInput(grade, input);
        return grade;
    }

    private void updateEntityFromInput(Grade grade, GradeInput input) {
        grade.setTenantId(input.getTenantId());
        grade.setName(input.getName());
        grade.setCode(input.getCode());
        grade.setLevel(input.getLevel());
        grade.setMinSalary(input.getMinSalary());
        grade.setMaxSalary(input.getMaxSalary());
        grade.setDescription(input.getDescription());
        grade.setIsActive(input.getIsActive());
    }
}
