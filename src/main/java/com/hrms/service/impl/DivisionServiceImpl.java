package com.hrms.service.impl;

import com.hrms.dto.request.DivisionRequest;
import com.hrms.entity.Division;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.DivisionRepository;
import com.hrms.service.DivisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Division operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DivisionServiceImpl implements DivisionService {

    private final DivisionRepository divisionRepository;

    @Override
    public List<Division> getAllDivisions() {
        log.debug("Fetching all divisions");
        return divisionRepository.findAll();
    }

    @Override
    public Division getDivisionById(Long id) {
        log.debug("Fetching division with id: {}", id);
        return divisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Division", "id", id));
    }

    @Override
    public List<Division> getDivisionsByTenant(String tenantId) {
        log.debug("Fetching divisions for tenant: {}", tenantId);
        return divisionRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Division> getActiveDivisions() {
        log.debug("Fetching active divisions");
        return divisionRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional
    public Division createDivision(DivisionRequest request) {
        log.debug("Creating new division: {}", request.getName());

        Optional<Division> existing = divisionRepository
                .findByTenantIdAndCode(request.getTenantId(), request.getCode());
        if (existing.isPresent()) {
            throw new DuplicateResourceException("Division", "code", request.getCode());
        }

        Division division = mapToEntity(request);
        Division saved = divisionRepository.save(division);

        log.info("Created division with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Division updateDivision(Long id, DivisionRequest request) {
        log.debug("Updating division with id: {}", id);

        Division existing = getDivisionById(id);

        Optional<Division> duplicate = divisionRepository
                .findByTenantIdAndCode(request.getTenantId(), request.getCode());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new DuplicateResourceException("Division", "code", request.getCode());
        }

        updateEntityFromRequest(existing, request);
        Division updated = divisionRepository.save(existing);

        log.info("Updated division with id: {}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteDivision(Long id) {
        log.debug("Deleting division with id: {}", id);

        if (!divisionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Division", "id", id);
        }
        divisionRepository.deleteById(id);

        log.info("Deleted division with id: {}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return divisionRepository.existsById(id);
    }

    private Division mapToEntity(DivisionRequest request) {
        Division division = new Division();
        division.setTenantId(request.getTenantId());
        division.setName(request.getName());
        division.setCode(request.getCode());
        division.setDescription(request.getDescription());
        division.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return division;
    }

    private void updateEntityFromRequest(Division division, DivisionRequest request) {
        division.setTenantId(request.getTenantId());
        division.setName(request.getName());
        division.setCode(request.getCode());
        division.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            division.setIsActive(request.getIsActive());
        }
    }
}
