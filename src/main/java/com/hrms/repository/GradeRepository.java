package com.hrms.repository;

import com.hrms.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByTenantId(String tenantId);

    Optional<Grade> findByTenantIdAndCode(String tenantId, String code);

    List<Grade> findByIsActiveTrue();

    List<Grade> findByTenantIdOrderByLevelAsc(String tenantId);
}
