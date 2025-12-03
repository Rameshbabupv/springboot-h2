package com.hrms.repository;

import com.hrms.entity.EmployeeTemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeTemplateVersionRepository extends JpaRepository<EmployeeTemplateVersion, Long> {

    List<EmployeeTemplateVersion> findByTemplateIdOrderByCreatedAtDesc(Long templateId);

    Optional<EmployeeTemplateVersion> findByTemplateIdAndVersion(Long templateId, String version);

    boolean existsByTemplateIdAndVersion(Long templateId, String version);
}
