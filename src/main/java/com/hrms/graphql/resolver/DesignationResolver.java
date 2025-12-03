package com.hrms.graphql.resolver;

import com.hrms.dto.request.DesignationRequest;
import com.hrms.entity.Designation;
import com.hrms.graphql.input.DesignationInput;
import com.hrms.service.DesignationService;
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
public class DesignationResolver {

    private final DesignationService designationService;

    @QueryMapping
    public List<Designation> designations() {
        return designationService.getAllDesignations();
    }

    @QueryMapping
    public Designation designation(@Argument Long id) {
        return designationService.getDesignationById(id);
    }

    @QueryMapping
    public List<Designation> designationsByTenant(@Argument String tenantId) {
        return designationService.getDesignationsByTenant(tenantId);
    }

    @QueryMapping
    public List<Designation> activeDesignationsByTenant(@Argument String tenantId) {
        return designationService.getActiveDesignationsByTenant(tenantId);
    }

    @QueryMapping
    public List<Designation> activeDesignations() {
        return designationService.getActiveDesignations();
    }

    @QueryMapping
    public List<Designation> searchDesignations(@Argument String tenantId, @Argument String searchTerm) {
        return designationService.searchDesignations(tenantId, searchTerm);
    }

    @MutationMapping
    public Designation createDesignation(@Argument DesignationInput input) {
        DesignationRequest request = mapToRequest(input);
        return designationService.createDesignation(request);
    }

    @MutationMapping
    public Designation updateDesignation(@Argument Long id, @Argument DesignationInput input) {
        DesignationRequest request = mapToRequest(input);
        return designationService.updateDesignation(id, request);
    }

    @MutationMapping
    public Boolean deleteDesignation(@Argument Long id) {
        designationService.deleteDesignation(id);
        return true;
    }

    private DesignationRequest mapToRequest(DesignationInput input) {
        return DesignationRequest.builder()
                .tenantId(input.getTenantId())
                .name(input.getName())
                .code(input.getCode())
                .description(input.getDescription())
                .isActive(input.getIsActive())
                .build();
    }
}
