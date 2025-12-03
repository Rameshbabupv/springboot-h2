package com.hrms.service.impl;

import com.hrms.dto.request.GradeRequest;
import com.hrms.entity.Grade;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.GradeRepository;
import com.hrms.service.GradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Grade operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;

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
    public List<Grade> getActiveGrades() {
        log.debug("Fetching active grades");
        return gradeRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional
    public Grade createGrade(GradeRequest request) {
        log.debug("Creating new grade: {}", request.getName());

        Optional<Grade> existing = gradeRepository
                .findByTenantIdAndCode(request.getTenantId(), request.getCode());
        if (existing.isPresent()) {
            throw new DuplicateResourceException("Grade", "code", request.getCode());
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

        Optional<Grade> duplicate = gradeRepository
                .findByTenantIdAndCode(request.getTenantId(), request.getCode());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new DuplicateResourceException("Grade", "code", request.getCode());
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

    private Grade mapToEntity(GradeRequest request) {
        Grade grade = new Grade();
        grade.setTenantId(request.getTenantId());
        grade.setName(request.getName());
        grade.setCode(request.getCode());
        grade.setLevel(request.getLevel());
        grade.setMinSalary(request.getMinSalary());
        grade.setMaxSalary(request.getMaxSalary());
        grade.setDescription(request.getDescription());
        grade.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return grade;
    }

    private void updateEntityFromRequest(Grade grade, GradeRequest request) {
        grade.setTenantId(request.getTenantId());
        grade.setName(request.getName());
        grade.setCode(request.getCode());
        grade.setLevel(request.getLevel());
        grade.setMinSalary(request.getMinSalary());
        grade.setMaxSalary(request.getMaxSalary());
        grade.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            grade.setIsActive(request.getIsActive());
        }
    }
}
