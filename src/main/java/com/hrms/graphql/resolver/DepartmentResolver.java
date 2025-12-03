package com.hrms.graphql.resolver;

import com.hrms.dto.request.DepartmentRequest;
import com.hrms.entity.Department;
import com.hrms.graphql.input.DepartmentInput;
import com.hrms.service.DepartmentService;
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
public class DepartmentResolver {

    private final DepartmentService departmentService;

    @QueryMapping
    public List<Department> departments() {
        log.debug("GraphQL Query: departments");
        List<Department> result = departmentService.getAllDepartments();
        log.info("GraphQL Response: departments - returned {} departments", result.size());
        return result;
    }

    @QueryMapping
    public Department department(@Argument Long id) {
        log.debug("GraphQL Query: department - id: {}", id);
        Department result = departmentService.getDepartmentById(id);
        log.info("GraphQL Response: department - returned: {}", result.getName());
        return result;
    }

    @QueryMapping
    public List<Department> departmentsByTenant(@Argument String tenantId) {
        log.debug("GraphQL Query: departmentsByTenant - tenantId: {}", tenantId);
        List<Department> result = departmentService.getDepartmentsByTenant(tenantId);
        log.info("GraphQL Response: departmentsByTenant - returned {} departments for tenant: {}", result.size(), tenantId);
        return result;
    }

    @QueryMapping
    public List<Department> activeDepartmentsByTenant(@Argument String tenantId) {
        log.debug("GraphQL Query: activeDepartmentsByTenant - tenantId: {}", tenantId);
        List<Department> result = departmentService.getActiveDepartmentsByTenant(tenantId);
        log.info("GraphQL Response: activeDepartmentsByTenant - returned {} active departments", result.size());
        return result;
    }

    @QueryMapping
    public List<Department> activeDepartments() {
        log.debug("GraphQL Query: activeDepartments");
        List<Department> result = departmentService.getActiveDepartments();
        log.info("GraphQL Response: activeDepartments - returned {} active departments", result.size());
        return result;
    }

    @QueryMapping
    public List<Department> searchDepartments(@Argument String tenantId, @Argument String searchTerm) {
        log.debug("GraphQL Query: searchDepartments - tenantId: {}, searchTerm: {}", tenantId, searchTerm);
        List<Department> result = departmentService.searchDepartments(tenantId, searchTerm);
        log.info("GraphQL Response: searchDepartments - returned {} departments matching: {}", result.size(), searchTerm);
        return result;
    }

    @MutationMapping
    public Department createDepartment(@Argument DepartmentInput input) {
        log.debug("GraphQL Mutation: createDepartment - name: {}, code: {}", input.getName(), input.getCode());
        DepartmentRequest request = mapToRequest(input);
        Department result = departmentService.createDepartment(request);
        log.info("GraphQL Response: createDepartment - created department id: {}, name: {}", result.getId(), result.getName());
        return result;
    }

    @MutationMapping
    public Department updateDepartment(@Argument Long id, @Argument DepartmentInput input) {
        log.debug("GraphQL Mutation: updateDepartment - id: {}", id);
        DepartmentRequest request = mapToRequest(input);
        Department result = departmentService.updateDepartment(id, request);
        log.info("GraphQL Response: updateDepartment - updated department id: {}, name: {}", result.getId(), result.getName());
        return result;
    }

    @MutationMapping
    public Boolean deleteDepartment(@Argument Long id) {
        log.debug("GraphQL Mutation: deleteDepartment - id: {}", id);
        departmentService.deleteDepartment(id);
        log.info("GraphQL Response: deleteDepartment - successfully deleted department id: {}", id);
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
