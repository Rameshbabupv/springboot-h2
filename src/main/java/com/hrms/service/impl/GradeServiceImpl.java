package com.hrms.service.impl;

import com.hrms.dto.request.GradeRequest;
import com.hrms.dto.request.OrganizationalScopeDTO;
import com.hrms.entity.Grade;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.GradeRepository;
import com.hrms.service.GradeService;
import com.hrms.service.OrganizationalScopeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Service implementation for Grade operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final OrganizationalScopeService organizationalScopeService;

    @Override
    public List<Grade> getAllGrades() {
        log.debug("Fetching all grades");
        return gradeRepository.findAll();
    }

    @Override
    public Grade getGradeById(Long id) {
        log.debug("Fetching grade with id: {}", id);
        return gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", id));
    }

    @Override
    public List<Grade> getGradesByTenant(String tenantId) {
        log.debug("Fetching grades for tenant: {}", tenantId);
        return gradeRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Grade> getActiveGradesByTenant(String tenantId) {
        log.debug("Fetching active grades for tenant: {}", tenantId);
        return gradeRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Override
    public List<Grade> getActiveGrades() {
        log.debug("Fetching active grades");
        return gradeRepository.findByIsActiveTrue();
    }

    @Override
    public List<Grade> searchGrades(String tenantId, String searchTerm) {
        log.debug("Searching grades for tenant: {} with term: {}", tenantId, searchTerm);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getActiveGradesByTenant(tenantId);
        }
        return gradeRepository.searchGrades(tenantId, searchTerm);
    }

    @Override
    @Transactional
    public Grade createGrade(GradeRequest request) {
        log.debug("Creating new grade: {}", request.getName());

        // Auto-convert code to uppercase
        String upperCode = request.getCode().toUpperCase();
        request.setCode(upperCode);

        // Case-insensitive duplicate check for code
        Optional<Grade> existingCode = gradeRepository
                .findByTenantIdAndCodeIgnoreCase(request.getTenantId(), request.getCode());
        if (existingCode.isPresent()) {
            throw new DuplicateResourceException("Grade", "code", request.getCode());
        }

        // Case-insensitive duplicate check for name
        Optional<Grade> existingName = gradeRepository
                .findByTenantIdAndNameIgnoreCase(request.getTenantId(), request.getName());
        if (existingName.isPresent()) {
            throw new DuplicateResourceException("Grade", "name", request.getName());
        }

        Grade grade = mapToEntity(request);
        Grade saved = gradeRepository.save(grade);

        log.info("Created grade with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Grade updateGrade(Long id, GradeRequest request) {
        log.debug("Updating grade with id: {}", id);

        Grade existing = getGradeById(id);

        // Auto-convert code to uppercase
        String upperCode = request.getCode().toUpperCase();
        request.setCode(upperCode);

        // Case-insensitive duplicate check for code
        Optional<Grade> duplicateCode = gradeRepository
                .findByTenantIdAndCodeIgnoreCase(request.getTenantId(), request.getCode());
        if (duplicateCode.isPresent() && !duplicateCode.get().getId().equals(id)) {
            throw new DuplicateResourceException("Grade", "code", request.getCode());
        }

        // Case-insensitive duplicate check for name
        Optional<Grade> duplicateName = gradeRepository
                .findByTenantIdAndNameIgnoreCase(request.getTenantId(), request.getName());
        if (duplicateName.isPresent() && !duplicateName.get().getId().equals(id)) {
            throw new DuplicateResourceException("Grade", "name", request.getName());
        }

        updateEntityFromRequest(existing, request);
        Grade updated = gradeRepository.save(existing);

        log.info("Updated grade with id: {}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteGrade(Long id) {
        log.debug("Deleting grade with id: {}", id);

        if (!gradeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Grade", "id", id);
        }
        gradeRepository.deleteById(id);

        log.info("Deleted grade with id: {}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return gradeRepository.existsById(id);
    }

    @Override
    public List<Grade> getGradesForSelection(String tenantId, Long userId, boolean isEditMode, Long currentGradeId) {
        log.debug("Fetching grades for selection - tenantId: {}, userId: {}, editMode: {}", tenantId, userId, isEditMode);

        OrganizationalScopeDTO userScope = organizationalScopeService.getUserScope(tenantId, userId);
        List<Long> allowedIds = null;

        if (userScope != null && userScope.getGradeIds() != null && !userScope.getGradeIds().isEmpty()) {
            allowedIds = userScope.getGradeIds();
        }

        List<Grade> scopedList;
        if (allowedIds == null || allowedIds.isEmpty()) {
            scopedList = gradeRepository.findByTenantIdAndIsActiveTrue(tenantId);
        } else {
            scopedList = gradeRepository.findByTenantIdAndIdInAndIsActiveTrue(tenantId, allowedIds);
        }

        if (isEditMode && currentGradeId != null) {
            boolean found = scopedList.stream().anyMatch(g -> g.getId().equals(currentGradeId));
            if (!found) {
                gradeRepository.findById(currentGradeId).ifPresent(scopedList::add);
            }
        }

        List<Grade> result = scopedList.stream()
                .sorted(Comparator.comparing(Grade::getName))
                .collect(Collectors.toList());

        log.info("Fetched {} grades for selection", result.size());
        return result;
    }

    private Grade mapToEntity(GradeRequest request) {
        Grade grade = new Grade();
        grade.setTenantId(request.getTenantId());
        grade.setName(request.getName());
        grade.setCode(request.getCode());
        grade.setDescription(request.getDescription());
        grade.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        grade.setCreatedBy(request.getCreatedBy());
        return grade;
    }

    private void updateEntityFromRequest(Grade grade, GradeRequest request) {
        grade.setTenantId(request.getTenantId());
        grade.setName(request.getName());
        grade.setCode(request.getCode());
        grade.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            grade.setIsActive(request.getIsActive());
        }
        grade.setUpdatedBy(request.getUpdatedBy());
    }
}
