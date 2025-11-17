package com.hrms.repository;

import com.hrms.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {

    List<Designation> findByTenantId(String tenantId);

    Optional<Designation> findByTenantIdAndCode(String tenantId, String code);

    List<Designation> findByIsActiveTrue();
}
