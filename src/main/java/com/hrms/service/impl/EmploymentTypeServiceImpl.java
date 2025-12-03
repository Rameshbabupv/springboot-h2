package com.hrms.service.impl;

import com.hrms.dto.request.EmploymentTypeRequest;
import com.hrms.entity.EmploymentType;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.EmploymentTypeRepository;
import com.hrms.service.EmploymentTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for EmploymentType operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmploymentTypeServiceImpl implements EmploymentTypeService {

    private final EmploymentTypeRepository employmentTypeRepository;

    @Override
    public List<EmploymentType> getAllEmploymentTypes() {
        log.debug("Fetching all employment types");
        return employmentTypeRepository.findAll();
    }

    @Override
    public EmploymentType getEmploymentTypeById(Long id) {
        log.debug("Fetching employment type with id: {}", id);
        return employmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmploymentType", "id", id));
    }

    @Override
    public List<EmploymentType> getEmploymentTypesByTenant(String tenantId) {
        log.debug("Fetching employment types for tenant: {}", tenantId);
        return employmentTypeRepository.findByTenantId(tenantId);
    }

    @Override
    public List<EmploymentType> getActiveEmploymentTypes() {
        log.debug("Fetching active employment types");
        return employmentTypeRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional
    public EmploymentType createEmploymentType(EmploymentTypeRequest request) {
        log.debug("Creating new employment type: {}", request.getName());

        Optional<EmploymentType> existing = employmentTypeRepository
                .findByTenantIdAndCode(request.getTenantId(), request.getCode());
        if (existing.isPresent()) {
            throw new DuplicateResourceException("EmploymentType", "code", request.getCode());
        }

        EmploymentType employmentType = mapToEntity(request);
        EmploymentType saved = employmentTypeRepository.save(employmentType);

        log.info("Created employment type with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public EmploymentType updateEmploymentType(Long id, EmploymentTypeRequest request) {
        log.debug("Updating employment type with id: {}", id);

        EmploymentType existing = getEmploymentTypeById(id);

        Optional<EmploymentType> duplicate = employmentTypeRepository
                .findByTenantIdAndCode(request.getTenantId(), request.getCode());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new DuplicateResourceException("EmploymentType", "code", request.getCode());
        }

        updateEntityFromRequest(existing, request);
        EmploymentType updated = employmentTypeRepository.save(existing);

        log.info("Updated employment type with id: {}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteEmploymentType(Long id) {
        log.debug("Deleting employment type with id: {}", id);

        if (!employmentTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("EmploymentType", "id", id);
        }
        employmentTypeRepository.deleteById(id);

        log.info("Deleted employment type with id: {}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return employmentTypeRepository.existsById(id);
    }

    private EmploymentType mapToEntity(EmploymentTypeRequest request) {
        EmploymentType employmentType = new EmploymentType();
        employmentType.setTenantId(request.getTenantId());
        employmentType.setName(request.getName());
        employmentType.setCode(request.getCode());
        employmentType.setDescription(request.getDescription());
        employmentType.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return employmentType;
    }

    private void updateEntityFromRequest(EmploymentType employmentType, EmploymentTypeRequest request) {
        employmentType.setTenantId(request.getTenantId());
        employmentType.setName(request.getName());
        employmentType.setCode(request.getCode());
        employmentType.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            employmentType.setIsActive(request.getIsActive());
        }
    }
}
