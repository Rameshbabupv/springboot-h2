package com.hrms.repository;

import com.hrms.entity.EmploymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmploymentTypeRepository extends JpaRepository<EmploymentType, Long> {

    List<EmploymentType> findByTenantId(String tenantId);

    Optional<EmploymentType> findByTenantIdAndCode(String tenantId, String code);

    List<EmploymentType> findByIsActiveTrue();
}
