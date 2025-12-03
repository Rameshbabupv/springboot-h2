package com.hrms.repository;

import com.hrms.entity.EmployeeTemplateSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeTemplateSectionRepository extends JpaRepository<EmployeeTemplateSection, Long> {

    List<EmployeeTemplateSection> findByTemplateIdOrderBySectionOrder(Long templateId);

    Optional<EmployeeTemplateSection> findByTemplateIdAndSectionCode(Long templateId, String sectionCode);

    boolean existsByTemplateIdAndSectionCode(Long templateId, String sectionCode);

    void deleteByTemplateId(Long templateId);
}
