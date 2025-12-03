package com.hrms.repository;

import com.hrms.entity.EmployeeTemplateField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeTemplateFieldRepository extends JpaRepository<EmployeeTemplateField, Long> {

    List<EmployeeTemplateField> findBySectionIdOrderByDisplayOrder(Long sectionId);

    List<EmployeeTemplateField> findByTemplateId(Long templateId);

    Optional<EmployeeTemplateField> findByTemplateIdAndSectionIdAndFieldId(Long templateId, Long sectionId, Long fieldId);

    boolean existsByTemplateIdAndSectionIdAndFieldId(Long templateId, Long sectionId, Long fieldId);

    void deleteByTemplateId(Long templateId);

    void deleteBySectionId(Long sectionId);
}
