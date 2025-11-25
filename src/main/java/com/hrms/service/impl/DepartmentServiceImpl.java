package com.hrms.service.impl;

import com.hrms.dto.request.DepartmentRequest;
import com.hrms.entity.Department;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.DepartmentRepository;
import com.hrms.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Department operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public List<Department> getAllDepartments() {
        log.debug("Fetching all departments");
        return departmentRepository.findAll();
    }

    @Override
    public Department getDepartmentById(Long id) {
        log.debug("Fetching department with id: {}", id);
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    @Override
    public List<Department> getDepartmentsByTenant(String tenantId) {
        log.debug("Fetching departments for tenant: {}", tenantId);
        return departmentRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Department> getActiveDepartments() {
        log.debug("Fetching active departments");
        return departmentRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional
    public Department createDepartment(DepartmentRequest request) {
        log.debug("Creating new department: {}", request.getName());

        // Check for duplicate code within tenant
        Optional<Department> existing = departmentRepository
                .findByTenantIdAndCode(request.getTenantId(), request.getCode());
        if (existing.isPresent()) {
            throw new DuplicateResourceException("Department", "code", request.getCode());
        }

        Department department = mapToEntity(request);
        Department saved = departmentRepository.save(department);

        log.info("Created department with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Department updateDepartment(Long id, DepartmentRequest request) {
        log.debug("Updating department with id: {}", id);

        Department existing = getDepartmentById(id);

        // Check for duplicate code within tenant (exclude current entity)
        Optional<Department> duplicate = departmentRepository
                .findByTenantIdAndCode(request.getTenantId(), request.getCode());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new DuplicateResourceException("Department", "code", request.getCode());
        }

        updateEntityFromRequest(existing, request);
        Department updated = departmentRepository.save(existing);

        log.info("Updated department with id: {}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        log.debug("Deleting department with id: {}", id);

        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department", "id", id);
        }
        departmentRepository.deleteById(id);

        log.info("Deleted department with id: {}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return departmentRepository.existsById(id);
    }

    private Department mapToEntity(DepartmentRequest request) {
        Department department = new Department();
        department.setTenantId(request.getTenantId());
        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setDescription(request.getDescription());
        department.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return department;
    }

    private void updateEntityFromRequest(Department department, DepartmentRequest request) {
        department.setTenantId(request.getTenantId());
        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            department.setIsActive(request.getIsActive());
        }
    }
}
