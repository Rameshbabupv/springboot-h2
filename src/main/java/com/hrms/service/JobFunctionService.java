package com.hrms.service;

import com.hrms.dto.request.JobFunctionRequest;
import com.hrms.entity.JobFunction;

import java.util.List;

/**
 * Service interface for JobFunction operations.
 */
public interface JobFunctionService {

    List<JobFunction> getAllJobFunctions();

    JobFunction getJobFunctionById(Long id);

    List<JobFunction> getJobFunctionsByTenant(String tenantId);

    List<JobFunction> getActiveJobFunctionsByTenant(String tenantId);

    List<JobFunction> getActiveJobFunctions();

    List<JobFunction> getJobFunctionsByGroup(String tenantId, String functionGroup);

    List<JobFunction> searchJobFunctions(String tenantId, String searchTerm);

    JobFunction createJobFunction(JobFunctionRequest request);

    JobFunction updateJobFunction(Long id, JobFunctionRequest request);

    void deleteJobFunction(Long id);

    boolean existsById(Long id);
}
