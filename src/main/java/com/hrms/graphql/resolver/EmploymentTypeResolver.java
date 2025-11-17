package com.hrms.graphql.resolver;

import com.hrms.entity.EmploymentType;
import com.hrms.graphql.input.EmploymentTypeInput;
import com.hrms.repository.EmploymentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EmploymentTypeResolver {

    private final EmploymentTypeRepository employmentTypeRepository;

    @QueryMapping
    public List<EmploymentType> employmentTypes() {
        return employmentTypeRepository.findAll();
    }

    @QueryMapping
    public EmploymentType employmentType(@Argument Long id) {
        return employmentTypeRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<EmploymentType> employmentTypesByTenant(@Argument String tenantId) {
        return employmentTypeRepository.findByTenantId(tenantId);
    }

    @QueryMapping
    public List<EmploymentType> activeEmploymentTypes() {
        return employmentTypeRepository.findByIsActiveTrue();
    }

    @MutationMapping
    public EmploymentType createEmploymentType(@Argument EmploymentTypeInput input) {
        EmploymentType employmentType = mapToEntity(input);
        return employmentTypeRepository.save(employmentType);
    }

    @MutationMapping
    public EmploymentType updateEmploymentType(@Argument Long id, @Argument EmploymentTypeInput input) {
        EmploymentType employmentType = employmentTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmploymentType not found"));

        updateEntityFromInput(employmentType, input);
        return employmentTypeRepository.save(employmentType);
    }

    @MutationMapping
    public Boolean deleteEmploymentType(@Argument Long id) {
        if (employmentTypeRepository.existsById(id)) {
            employmentTypeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private EmploymentType mapToEntity(EmploymentTypeInput input) {
        EmploymentType employmentType = new EmploymentType();
        updateEntityFromInput(employmentType, input);
        return employmentType;
    }

    private void updateEntityFromInput(EmploymentType employmentType, EmploymentTypeInput input) {
        employmentType.setTenantId(input.getTenantId());
        employmentType.setName(input.getName());
        employmentType.setCode(input.getCode());
        employmentType.setDescription(input.getDescription());
        employmentType.setIsActive(input.getIsActive());
    }
}
