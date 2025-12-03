package com.hrms.service.impl;

import com.hrms.dto.request.DesignationRequest;
import com.hrms.entity.Designation;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.DesignationRepository;
import com.hrms.service.DesignationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Designation operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;

    @Override
    public List<Designation> getAllDesignations() {
        log.debug("Fetching all designations");
        return designationRepository.findAll();
    }

    @Override
    public Designation getDesignationById(Long id) {
        log.debug("Fetching designation with id: {}", id);
        return designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", id));
    }

    @Override
    public List<Designation> getDesignationsByTenant(String tenantId) {
        log.debug("Fetching designations for tenant: {}", tenantId);
        return designationRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Designation> getActiveDesignations() {
        log.debug("Fetching active designations");
        return designationRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional
    public Designation createDesignation(DesignationRequest request) {
        log.debug("Creating new designation: {}", request.getName());

        // Check for duplicate code within tenant
        Optional<Designation> existing = designationRepository
                .findByTenantIdAndCode(request.getTenantId(), request.getCode());
        if (existing.isPresent()) {
            throw new DuplicateResourceException("Designation", "code", request.getCode());
        }

        Designation designation = mapToEntity(request);
        Designation saved = designationRepository.save(designation);

        log.info("Created designation with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Designation updateDesignation(Long id, DesignationRequest request) {
        log.debug("Updating designation with id: {}", id);

        Designation existing = getDesignationById(id);

        // Check for duplicate code within tenant (exclude current entity)
        Optional<Designation> duplicate = designationRepository
                .findByTenantIdAndCode(request.getTenantId(), request.getCode());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new DuplicateResourceException("Designation", "code", request.getCode());
        }

        updateEntityFromRequest(existing, request);
        Designation updated = designationRepository.save(existing);

        log.info("Updated designation with id: {}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteDesignation(Long id) {
        log.debug("Deleting designation with id: {}", id);

        if (!designationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Designation", "id", id);
        }
        designationRepository.deleteById(id);

        log.info("Deleted designation with id: {}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return designationRepository.existsById(id);
    }

    private Designation mapToEntity(DesignationRequest request) {
        Designation designation = new Designation();
        designation.setTenantId(request.getTenantId());
        designation.setName(request.getName());
        designation.setCode(request.getCode());
        designation.setDescription(request.getDescription());
        designation.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return designation;
    }

    private void updateEntityFromRequest(Designation designation, DesignationRequest request) {
        designation.setTenantId(request.getTenantId());
        designation.setName(request.getName());
        designation.setCode(request.getCode());
        designation.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            designation.setIsActive(request.getIsActive());
        }
    }
}
