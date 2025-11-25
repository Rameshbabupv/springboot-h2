package com.hrms.service.impl;

import com.hrms.dto.request.SectionRequest;
import com.hrms.entity.Department;
import com.hrms.entity.Section;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.SectionRepository;
import com.hrms.service.SectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Section operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public List<Section> getAllSections() {
        log.debug("Fetching all sections");
        return sectionRepository.findAll();
    }

    @Override
    public Section getSectionById(Long id) {
        log.debug("Fetching section with id: {}", id);
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));
    }

    @Override
    public List<Section> getSectionsByTenant(String tenantId) {
        log.debug("Fetching sections for tenant: {}", tenantId);
        return sectionRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Section> getSectionsByDepartment(Long departmentId) {
        log.debug("Fetching sections for department: {}", departmentId);
        return sectionRepository.findByDepartmentId(departmentId);
    }

    @Override
    public List<Section> getActiveSections() {
        log.debug("Fetching active sections");
        return sectionRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional
    public Section createSection(SectionRequest request) {
        log.debug("Creating new section: {}", request.getName());

        Optional<Section> existing = sectionRepository
                .findByTenantIdAndCode(request.getTenantId(), request.getCode());
        if (existing.isPresent()) {
            throw new DuplicateResourceException("Section", "code", request.getCode());
        }

        Section section = mapToEntity(request);
        Section saved = sectionRepository.save(section);

        log.info("Created section with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Section updateSection(Long id, SectionRequest request) {
        log.debug("Updating section with id: {}", id);

        Section existing = getSectionById(id);

        Optional<Section> duplicate = sectionRepository
                .findByTenantIdAndCode(request.getTenantId(), request.getCode());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new DuplicateResourceException("Section", "code", request.getCode());
        }

        updateEntityFromRequest(existing, request);
        Section updated = sectionRepository.save(existing);

        log.info("Updated section with id: {}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        log.debug("Deleting section with id: {}", id);

        if (!sectionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Section", "id", id);
        }
        sectionRepository.deleteById(id);

        log.info("Deleted section with id: {}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return sectionRepository.existsById(id);
    }

    private Section mapToEntity(SectionRequest request) {
        Section section = new Section();
        section.setTenantId(request.getTenantId());

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
        section.setDepartment(department);

        section.setName(request.getName());
        section.setCode(request.getCode());
        section.setDescription(request.getDescription());
        section.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return section;
    }

    private void updateEntityFromRequest(Section section, SectionRequest request) {
        section.setTenantId(request.getTenantId());

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
        section.setDepartment(department);

        section.setName(request.getName());
        section.setCode(request.getCode());
        section.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            section.setIsActive(request.getIsActive());
        }
    }
}
