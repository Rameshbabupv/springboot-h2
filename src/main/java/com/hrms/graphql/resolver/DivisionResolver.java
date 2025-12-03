package com.hrms.graphql.resolver;

import com.hrms.dto.request.DivisionRequest;
import com.hrms.entity.Division;
import com.hrms.graphql.input.DivisionInput;
import com.hrms.service.DivisionService;
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
public class DivisionResolver {

    private final DivisionService divisionService;

    @QueryMapping
    public List<Division> divisions() {
        return divisionService.getAllDivisions();
    }

    @QueryMapping
    public Division division(@Argument Long id) {
        return divisionService.getDivisionById(id);
    }

    @QueryMapping
    public List<Division> divisionsByTenant(@Argument String tenantId) {
        return divisionService.getDivisionsByTenant(tenantId);
    }

    @QueryMapping
    public List<Division> activeDivisionsByTenant(@Argument String tenantId) {
        return divisionService.getActiveDivisionsByTenant(tenantId);
    }

    @QueryMapping
    public List<Division> activeDivisions() {
        return divisionService.getActiveDivisions();
    }

    @QueryMapping
    public List<Division> searchDivisions(@Argument String tenantId, @Argument String searchTerm) {
        return divisionService.searchDivisions(tenantId, searchTerm);
    }

    @MutationMapping
    public Division createDivision(@Argument DivisionInput input) {
        DivisionRequest request = mapToRequest(input);
        return divisionService.createDivision(request);
    }

    @MutationMapping
    public Division updateDivision(@Argument Long id, @Argument DivisionInput input) {
        DivisionRequest request = mapToRequest(input);
        return divisionService.updateDivision(id, request);
    }

    @MutationMapping
    public Boolean deleteDivision(@Argument Long id) {
        divisionService.deleteDivision(id);
        return true;
    }

    private DivisionRequest mapToRequest(DivisionInput input) {
        return DivisionRequest.builder()
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
