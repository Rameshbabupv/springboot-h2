package com.hrms.repository;

import com.hrms.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByTenantId(String tenantId);

    Optional<Section> findByTenantIdAndCode(String tenantId, String code);

    List<Section> findByDepartmentId(Long departmentId);

    List<Section> findByIsActiveTrue();
}
