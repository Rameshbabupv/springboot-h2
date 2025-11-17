package com.hrms.repository;

import com.hrms.entity.Division;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Long> {

    List<Division> findByTenantId(String tenantId);

    Optional<Division> findByTenantIdAndCode(String tenantId, String code);

    List<Division> findByIsActiveTrue();
}
