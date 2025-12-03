package com.hrms.graphql.resolver;

import com.hrms.dto.request.EmploymentTypeRequest;
import com.hrms.entity.EmploymentType;
import com.hrms.graphql.input.EmploymentTypeInput;
import com.hrms.service.EmploymentTypeService;
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
public class EmploymentTypeResolver {

    private final EmploymentTypeService employmentTypeService;

    @QueryMapping
    public List<EmploymentType> employmentTypes() {
        return employmentTypeService.getAllEmploymentTypes();
    }

    @QueryMapping
    public EmploymentType employmentType(@Argument Long id) {
        return employmentTypeService.getEmploymentTypeById(id);
    }

    @QueryMapping
    public List<EmploymentType> employmentTypesByTenant(@Argument String tenantId) {
        return employmentTypeService.getEmploymentTypesByTenant(tenantId);
    }

    @QueryMapping
    public List<EmploymentType> activeEmploymentTypes() {
        return employmentTypeService.getActiveEmploymentTypes();
    }

    @QueryMapping
    public List<EmploymentType> searchEmploymentTypes(@Argument String tenantId, @Argument String searchTerm) {
        return employmentTypeService.searchEmploymentTypes(tenantId, searchTerm);
    }

    @MutationMapping
    public EmploymentType createEmploymentType(@Argument EmploymentTypeInput input) {
        EmploymentTypeRequest request = mapToRequest(input);
        return employmentTypeService.createEmploymentType(request);
    }

    @MutationMapping
    public EmploymentType updateEmploymentType(@Argument Long id, @Argument EmploymentTypeInput input) {
        EmploymentTypeRequest request = mapToRequest(input);
        return employmentTypeService.updateEmploymentType(id, request);
    }

    @MutationMapping
    public Boolean deleteEmploymentType(@Argument Long id) {
        employmentTypeService.deleteEmploymentType(id);
        return true;
    }

    private EmploymentTypeRequest mapToRequest(EmploymentTypeInput input) {
        return EmploymentTypeRequest.builder()
                .tenantId(input.getTenantId())
                .name(input.getName())
                .code(input.getCode())
                .description(input.getDescription())
                .isActive(input.getIsActive())
                .build();
    }
}
