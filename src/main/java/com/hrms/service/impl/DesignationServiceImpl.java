package com.hrms.service.impl;

import com.hrms.dto.request.DesignationRequest;
import com.hrms.dto.request.OrganizationalScopeDTO;
import com.hrms.entity.Designation;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.DesignationRepository;
import com.hrms.service.DesignationService;
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
 * Service implementation for Designation operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final OrganizationalScopeService organizationalScopeService;

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
    public List<Designation> getActiveDesignationsByTenant(String tenantId) {
        log.debug("Fetching active designations for tenant: {}", tenantId);
        return designationRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Override
    public List<Designation> getActiveDesignations() {
        log.debug("Fetching active designations");
        return designationRepository.findByIsActiveTrue();
    }

    @Override
    public List<Designation> searchDesignations(String tenantId, String searchTerm) {
        log.debug("Searching designations for tenant: {} with term: {}", tenantId, searchTerm);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getActiveDesignationsByTenant(tenantId);
        }
        return designationRepository.searchDesignations(tenantId, searchTerm);
    }

    @Override
    @Transactional
    public Designation createDesignation(DesignationRequest request) {
        log.debug("Creating new designation: {}", request.getName());

        // Auto-convert code to uppercase
        String upperCode = request.getCode().toUpperCase();
        request.setCode(upperCode);

        // Case-insensitive duplicate check for code
        Optional<Designation> existingCode = designationRepository
                .findByTenantIdAndCodeIgnoreCase(request.getTenantId(), request.getCode());
        if (existingCode.isPresent()) {
            throw new DuplicateResourceException("Designation", "code", request.getCode());
        }

        // Case-insensitive duplicate check for name
        Optional<Designation> existingName = designationRepository
                .findByTenantIdAndNameIgnoreCase(request.getTenantId(), request.getName());
        if (existingName.isPresent()) {
            throw new DuplicateResourceException("Designation", "name", request.getName());
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

        // Auto-convert code to uppercase
        String upperCode = request.getCode().toUpperCase();
        request.setCode(upperCode);

        // Case-insensitive duplicate check for code (exclude current entity)
        Optional<Designation> duplicateCode = designationRepository
                .findByTenantIdAndCodeIgnoreCase(request.getTenantId(), request.getCode());
        if (duplicateCode.isPresent() && !duplicateCode.get().getId().equals(id)) {
            throw new DuplicateResourceException("Designation", "code", request.getCode());
        }

        // Case-insensitive duplicate check for name (exclude current entity)
        Optional<Designation> duplicateName = designationRepository
                .findByTenantIdAndNameIgnoreCase(request.getTenantId(), request.getName());
        if (duplicateName.isPresent() && !duplicateName.get().getId().equals(id)) {
            throw new DuplicateResourceException("Designation", "name", request.getName());
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

    @Override
    public List<Designation> getDesignationsForSelection(String tenantId, Long userId, boolean isEditMode, Long currentDesignationId) {
        log.debug("Fetching designations for selection - tenantId: {}, userId: {}, editMode: {}", tenantId, userId, isEditMode);

        // Get user's organizational scope
        OrganizationalScopeDTO userScope = organizationalScopeService.getUserScope(tenantId, userId);
        List<Long> allowedIds = null;

        if (userScope != null && userScope.getDesignationIds() != null && !userScope.getDesignationIds().isEmpty()) {
            allowedIds = userScope.getDesignationIds();
        }

        // Fetch scoped designations
        List<Designation> scopedList;
        if (allowedIds == null || allowedIds.isEmpty()) {
            // Super admin - get all active designations
            scopedList = designationRepository.findByTenantIdAndIsActiveTrue(tenantId);
        } else {
            // Scoped user - get only allowed designations
            scopedList = designationRepository.findByTenantIdAndIdInAndIsActiveTrue(tenantId, allowedIds);
        }

        // In edit mode, include current value even if outside scope
        if (isEditMode && currentDesignationId != null) {
            boolean found = scopedList.stream().anyMatch(d -> d.getId().equals(currentDesignationId));
            if (!found) {
                designationRepository.findById(currentDesignationId).ifPresent(scopedList::add);
            }
        }

        // Sort by name
        List<Designation> result = scopedList.stream()
                .sorted(Comparator.comparing(Designation::getName))
                .collect(Collectors.toList());

        log.info("Fetched {} designations for selection", result.size());
        return result;
    }

    private Designation mapToEntity(DesignationRequest request) {
        Designation designation = new Designation();
        designation.setTenantId(request.getTenantId());
        designation.setName(request.getName());
        designation.setCode(request.getCode());
        designation.setDescription(request.getDescription());
        designation.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        designation.setCreatedBy(request.getCreatedBy());
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
        designation.setUpdatedBy(request.getUpdatedBy());
    }
}
