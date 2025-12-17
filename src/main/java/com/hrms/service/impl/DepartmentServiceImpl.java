package com.hrms.service.impl;

import com.hrms.dto.request.DepartmentRequest;
import com.hrms.dto.request.OrganizationalScopeDTO;
import com.hrms.entity.Department;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.DepartmentRepository;
import com.hrms.service.DepartmentService;
import com.hrms.service.OrganizationalScopeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for Department operations with comprehensive validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final OrganizationalScopeService organizationalScopeService;

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
    public List<Department> getActiveDepartmentsByTenant(String tenantId) {
        log.debug("Fetching active departments for tenant: {}", tenantId);
        return departmentRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Override
    public List<Department> getActiveDepartments() {
        log.debug("Fetching active departments");
        return departmentRepository.findByIsActiveTrue();
    }

    @Override
    public List<Department> searchDepartments(String tenantId, String searchTerm) {
        log.debug("Searching departments for tenant: {} with term: {}", tenantId, searchTerm);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getActiveDepartmentsByTenant(tenantId);
        }
        return departmentRepository.searchDepartments(tenantId, searchTerm.trim());
    }

    @Override
    @Transactional
    public Department createDepartment(DepartmentRequest request) {
        log.debug("Creating new department: {}", request.getName());

        // Auto-convert code to uppercase
        String upperCode = request.getCode().toUpperCase();
        request.setCode(upperCode);

        // Case-insensitive duplicate check for code
        Optional<Department> existingCode = departmentRepository
                .findByTenantIdAndCodeIgnoreCase(request.getTenantId(), request.getCode());
        if (existingCode.isPresent()) {
            throw new DuplicateResourceException("Department", "code", request.getCode());
        }

        // Case-insensitive duplicate check for name
        Optional<Department> existingName = departmentRepository
                .findByTenantIdAndNameIgnoreCase(request.getTenantId(), request.getName());
        if (existingName.isPresent()) {
            throw new DuplicateResourceException("Department", "name", request.getName());
        }

        Department department = mapToEntity(request);
        Department saved = departmentRepository.save(department);

        log.info("Created department with id: {} for tenant: {}", saved.getId(), saved.getTenantId());
        return saved;
    }

    @Override
    @Transactional
    public Department updateDepartment(Long id, DepartmentRequest request) {
        log.debug("Updating department with id: {}", id);

        Department existing = getDepartmentById(id);

        // Auto-convert code to uppercase
        String upperCode = request.getCode().toUpperCase();
        request.setCode(upperCode);

        // Case-insensitive duplicate check for code (excluding current record)
        Optional<Department> duplicateCode = departmentRepository
                .findByTenantIdAndCodeIgnoreCase(request.getTenantId(), request.getCode());
        if (duplicateCode.isPresent() && !duplicateCode.get().getId().equals(id)) {
            throw new DuplicateResourceException("Department", "code", request.getCode());
        }

        // Case-insensitive duplicate check for name (excluding current record)
        Optional<Department> duplicateName = departmentRepository
                .findByTenantIdAndNameIgnoreCase(request.getTenantId(), request.getName());
        if (duplicateName.isPresent() && !duplicateName.get().getId().equals(id)) {
            throw new DuplicateResourceException("Department", "name", request.getName());
        }

        updateEntityFromRequest(existing, request);
        Department updated = departmentRepository.save(existing);

        log.info("Updated department with id: {} for tenant: {}", id, updated.getTenantId());
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

    @Override
    public List<Department> getDepartmentsForSelection(String tenantId, Long userId, boolean isEditMode, Long currentDepartmentId) {
        log.debug("Fetching departments for selection - tenantId: {}, userId: {}, editMode: {}", tenantId, userId, isEditMode);

        // Get user's organizational scope
        OrganizationalScopeDTO userScope = organizationalScopeService.getUserScope(tenantId, userId);
        List<Long> allowedIds = null;

        if (userScope != null && userScope.getDepartmentIds() != null && !userScope.getDepartmentIds().isEmpty()) {
            allowedIds = userScope.getDepartmentIds();
        }

        // Fetch scoped departments
        List<Department> scopedList;
        if (allowedIds == null || allowedIds.isEmpty()) {
            // Super admin - get all active departments
            scopedList = departmentRepository.findByTenantIdAndIsActiveTrue(tenantId);
        } else {
            // Scoped user - get only allowed departments
            scopedList = departmentRepository.findByTenantIdAndIdInAndIsActiveTrue(tenantId, allowedIds);
        }

        // In edit mode, include current value even if outside scope
        if (isEditMode && currentDepartmentId != null) {
            boolean found = scopedList.stream().anyMatch(d -> d.getId().equals(currentDepartmentId));
            if (!found) {
                departmentRepository.findById(currentDepartmentId).ifPresent(scopedList::add);
            }
        }

        // Sort by name
        List<Department> result = scopedList.stream()
                .sorted(Comparator.comparing(Department::getName))
                .collect(Collectors.toList());

        log.info("Fetched {} departments for selection", result.size());
        return result;
    }

    private Department mapToEntity(DepartmentRequest request) {
        Department department = new Department();
        department.setTenantId(request.getTenantId());
        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setDescription(request.getDescription());
        department.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        department.setCreatedBy(request.getCreatedBy());
        department.setUpdatedBy(request.getUpdatedBy());
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
        department.setUpdatedBy(request.getUpdatedBy());
    }
}
