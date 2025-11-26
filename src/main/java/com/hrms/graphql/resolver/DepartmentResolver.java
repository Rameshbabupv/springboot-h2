package com.hrms.graphql.resolver;

import com.hrms.dto.request.DepartmentRequest;
import com.hrms.entity.Department;
import com.hrms.graphql.input.DepartmentInput;
import com.hrms.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DepartmentResolver {

    private final DepartmentService departmentService;

    @QueryMapping
    public List<Department> departments() {
        return departmentService.getAllDepartments();
    }

    @QueryMapping
    public Department department(@Argument Long id) {
        return departmentService.getDepartmentById(id);
    }

    @QueryMapping
    public List<Department> departmentsByTenant(@Argument String tenantId) {
        return departmentService.getDepartmentsByTenant(tenantId);
    }

    @QueryMapping
    public List<Department> activeDepartmentsByTenant(@Argument String tenantId) {
        return departmentService.getActiveDepartmentsByTenant(tenantId);
    }

    @QueryMapping
    public List<Department> activeDepartments() {
        return departmentService.getActiveDepartments();
    }

    @QueryMapping
    public List<Department> searchDepartments(@Argument String tenantId, @Argument String searchTerm) {
        return departmentService.searchDepartments(tenantId, searchTerm);
    }

    @MutationMapping
    public Department createDepartment(@Argument DepartmentInput input) {
        DepartmentRequest request = mapToRequest(input);
        return departmentService.createDepartment(request);
    }

    @MutationMapping
    public Department updateDepartment(@Argument Long id, @Argument DepartmentInput input) {
        DepartmentRequest request = mapToRequest(input);
        return departmentService.updateDepartment(id, request);
    }

    @MutationMapping
    public Boolean deleteDepartment(@Argument Long id) {
        departmentService.deleteDepartment(id);
        return true;
    }

    private DepartmentRequest mapToRequest(DepartmentInput input) {
        return DepartmentRequest.builder()
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
