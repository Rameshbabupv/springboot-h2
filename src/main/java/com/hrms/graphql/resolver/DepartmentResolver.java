package com.hrms.graphql.resolver;

import com.hrms.entity.Department;
import com.hrms.graphql.input.DepartmentInput;
import com.hrms.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DepartmentResolver {

    private final DepartmentRepository departmentRepository;

    @QueryMapping
    public List<Department> departments() {
        return departmentRepository.findAll();
    }

    @QueryMapping
    public Department department(@Argument Long id) {
        return departmentRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Department> departmentsByTenant(@Argument String tenantId) {
        return departmentRepository.findByTenantId(tenantId);
    }

    @QueryMapping
    public List<Department> activeDepartments() {
        return departmentRepository.findByIsActiveTrue();
    }

    @MutationMapping
    public Department createDepartment(@Argument DepartmentInput input) {
        Department department = mapToEntity(input);
        return departmentRepository.save(department);
    }

    @MutationMapping
    public Department updateDepartment(@Argument Long id, @Argument DepartmentInput input) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        updateEntityFromInput(department, input);
        return departmentRepository.save(department);
    }

    @MutationMapping
    public Boolean deleteDepartment(@Argument Long id) {
        if (departmentRepository.existsById(id)) {
            departmentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Department mapToEntity(DepartmentInput input) {
        Department department = new Department();
        updateEntityFromInput(department, input);
        return department;
    }

    private void updateEntityFromInput(Department department, DepartmentInput input) {
        department.setTenantId(input.getTenantId());
        department.setName(input.getName());
        department.setCode(input.getCode());
        department.setDescription(input.getDescription());
        department.setIsActive(input.getIsActive());
    }
}
