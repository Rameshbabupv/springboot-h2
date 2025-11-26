package com.hrms.service.impl;

import com.hrms.dto.request.JobFunctionRequest;
import com.hrms.entity.JobFunction;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.JobFunctionRepository;
import com.hrms.service.JobFunctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for JobFunction operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobFunctionServiceImpl implements JobFunctionService {

    private final JobFunctionRepository jobFunctionRepository;

    @Override
    public List<JobFunction> getAllJobFunctions() {
        log.debug("Fetching all job functions");
        return jobFunctionRepository.findAll();
    }

    @Override
    public JobFunction getJobFunctionById(Long id) {
        log.debug("Fetching job function with id: {}", id);
        return jobFunctionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobFunction", "id", id));
    }

    @Override
    public List<JobFunction> getJobFunctionsByTenant(String tenantId) {
        log.debug("Fetching job functions for tenant: {}", tenantId);
        return jobFunctionRepository.findByTenantId(tenantId);
    }

    @Override
    public List<JobFunction> getActiveJobFunctionsByTenant(String tenantId) {
        log.debug("Fetching active job functions for tenant: {}", tenantId);
        return jobFunctionRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Override
    public List<JobFunction> getActiveJobFunctions() {
        log.debug("Fetching active job functions");
        return jobFunctionRepository.findByIsActiveTrue();
    }

    @Override
    public List<JobFunction> getJobFunctionsByGroup(String tenantId, String functionGroup) {
        log.debug("Fetching job functions for tenant: {} and group: {}", tenantId, functionGroup);
        return jobFunctionRepository.findByTenantIdAndFunctionGroup(tenantId, functionGroup);
    }

    @Override
    public List<JobFunction> searchJobFunctions(String tenantId, String searchTerm) {
        log.debug("Searching job functions for tenant: {} with term: {}", tenantId, searchTerm);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getActiveJobFunctionsByTenant(tenantId);
        }
        return jobFunctionRepository.searchJobFunctions(tenantId, searchTerm);
    }

    @Override
    @Transactional
    public JobFunction createJobFunction(JobFunctionRequest request) {
        log.debug("Creating new job function: {}", request.getName());

        // Auto-convert code to uppercase
        String upperCode = request.getCode().toUpperCase();
        request.setCode(upperCode);

        // Case-insensitive duplicate check for code
        Optional<JobFunction> existingCode = jobFunctionRepository
                .findByTenantIdAndCodeIgnoreCase(request.getTenantId(), request.getCode());
        if (existingCode.isPresent()) {
            throw new DuplicateResourceException("JobFunction", "code", request.getCode());
        }

        // Case-insensitive duplicate check for name
        Optional<JobFunction> existingName = jobFunctionRepository
                .findByTenantIdAndNameIgnoreCase(request.getTenantId(), request.getName());
        if (existingName.isPresent()) {
            throw new DuplicateResourceException("JobFunction", "name", request.getName());
        }

        JobFunction jobFunction = mapToEntity(request);
        JobFunction saved = jobFunctionRepository.save(jobFunction);

        log.info("Created job function with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public JobFunction updateJobFunction(Long id, JobFunctionRequest request) {
        log.debug("Updating job function with id: {}", id);

        JobFunction existing = getJobFunctionById(id);

        // Auto-convert code to uppercase
        String upperCode = request.getCode().toUpperCase();
        request.setCode(upperCode);

        // Case-insensitive duplicate check for code (exclude current entity)
        Optional<JobFunction> duplicateCode = jobFunctionRepository
                .findByTenantIdAndCodeIgnoreCase(request.getTenantId(), request.getCode());
        if (duplicateCode.isPresent() && !duplicateCode.get().getId().equals(id)) {
            throw new DuplicateResourceException("JobFunction", "code", request.getCode());
        }

        // Case-insensitive duplicate check for name (exclude current entity)
        Optional<JobFunction> duplicateName = jobFunctionRepository
                .findByTenantIdAndNameIgnoreCase(request.getTenantId(), request.getName());
        if (duplicateName.isPresent() && !duplicateName.get().getId().equals(id)) {
            throw new DuplicateResourceException("JobFunction", "name", request.getName());
        }

        updateEntityFromRequest(existing, request);
        JobFunction updated = jobFunctionRepository.save(existing);

        log.info("Updated job function with id: {}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteJobFunction(Long id) {
        log.debug("Deleting job function with id: {}", id);

        if (!jobFunctionRepository.existsById(id)) {
            throw new ResourceNotFoundException("JobFunction", "id", id);
        }
        jobFunctionRepository.deleteById(id);

        log.info("Deleted job function with id: {}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return jobFunctionRepository.existsById(id);
    }

    private JobFunction mapToEntity(JobFunctionRequest request) {
        JobFunction jobFunction = new JobFunction();
        jobFunction.setTenantId(request.getTenantId());
        jobFunction.setName(request.getName());
        jobFunction.setCode(request.getCode());
        jobFunction.setDescription(request.getDescription());
        jobFunction.setFunctionGroup(request.getFunctionGroup());
        jobFunction.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        jobFunction.setCreatedBy(request.getCreatedBy());
        return jobFunction;
    }

    private void updateEntityFromRequest(JobFunction jobFunction, JobFunctionRequest request) {
        jobFunction.setTenantId(request.getTenantId());
        jobFunction.setName(request.getName());
        jobFunction.setCode(request.getCode());
        jobFunction.setDescription(request.getDescription());
        jobFunction.setFunctionGroup(request.getFunctionGroup());
        if (request.getIsActive() != null) {
            jobFunction.setIsActive(request.getIsActive());
        }
        jobFunction.setUpdatedBy(request.getUpdatedBy());
    }
}
