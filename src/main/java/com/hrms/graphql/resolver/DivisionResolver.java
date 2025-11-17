package com.hrms.graphql.resolver;

import com.hrms.entity.Division;
import com.hrms.graphql.input.DivisionInput;
import com.hrms.repository.DivisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DivisionResolver {

    private final DivisionRepository divisionRepository;

    @QueryMapping
    public List<Division> divisions() {
        return divisionRepository.findAll();
    }

    @QueryMapping
    public Division division(@Argument Long id) {
        return divisionRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Division> divisionsByTenant(@Argument String tenantId) {
        return divisionRepository.findByTenantId(tenantId);
    }

    @QueryMapping
    public List<Division> activeDivisions() {
        return divisionRepository.findByIsActiveTrue();
    }

    @MutationMapping
    public Division createDivision(@Argument DivisionInput input) {
        Division division = mapToEntity(input);
        return divisionRepository.save(division);
    }

    @MutationMapping
    public Division updateDivision(@Argument Long id, @Argument DivisionInput input) {
        Division division = divisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Division not found"));

        updateEntityFromInput(division, input);
        return divisionRepository.save(division);
    }

    @MutationMapping
    public Boolean deleteDivision(@Argument Long id) {
        if (divisionRepository.existsById(id)) {
            divisionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Division mapToEntity(DivisionInput input) {
        Division division = new Division();
        updateEntityFromInput(division, input);
        return division;
    }

    private void updateEntityFromInput(Division division, DivisionInput input) {
        division.setTenantId(input.getTenantId());
        division.setName(input.getName());
        division.setCode(input.getCode());
        division.setDescription(input.getDescription());
        division.setIsActive(input.getIsActive());
    }
}
