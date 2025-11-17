package com.hrms.repository;

import com.hrms.entity.JobFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobFunctionRepository extends JpaRepository<JobFunction, Long> {

    List<JobFunction> findByTenantId(String tenantId);

    Optional<JobFunction> findByTenantIdAndCode(String tenantId, String code);

    List<JobFunction> findByIsActiveTrue();
}
