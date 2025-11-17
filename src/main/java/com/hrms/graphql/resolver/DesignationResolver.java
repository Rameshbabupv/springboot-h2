package com.hrms.graphql.resolver;

import com.hrms.entity.Designation;
import com.hrms.graphql.input.DesignationInput;
import com.hrms.repository.DesignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DesignationResolver {

    private final DesignationRepository designationRepository;

    @QueryMapping
    public List<Designation> designations() {
        return designationRepository.findAll();
    }

    @QueryMapping
    public Designation designation(@Argument Long id) {
        return designationRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Designation> designationsByTenant(@Argument String tenantId) {
        return designationRepository.findByTenantId(tenantId);
    }

    @QueryMapping
    public List<Designation> activeDesignations() {
        return designationRepository.findByIsActiveTrue();
    }

    @MutationMapping
    public Designation createDesignation(@Argument DesignationInput input) {
        Designation designation = mapToEntity(input);
        return designationRepository.save(designation);
    }

    @MutationMapping
    public Designation updateDesignation(@Argument Long id, @Argument DesignationInput input) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Designation not found"));

        updateEntityFromInput(designation, input);
        return designationRepository.save(designation);
    }

    @MutationMapping
    public Boolean deleteDesignation(@Argument Long id) {
        if (designationRepository.existsById(id)) {
            designationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Designation mapToEntity(DesignationInput input) {
        Designation designation = new Designation();
        updateEntityFromInput(designation, input);
        return designation;
    }

    private void updateEntityFromInput(Designation designation, DesignationInput input) {
        designation.setTenantId(input.getTenantId());
        designation.setName(input.getName());
        designation.setCode(input.getCode());
        designation.setDescription(input.getDescription());
        designation.setIsActive(input.getIsActive());
    }
}
