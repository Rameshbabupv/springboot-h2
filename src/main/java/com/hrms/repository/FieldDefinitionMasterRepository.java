package com.hrms.repository;

import com.hrms.entity.FieldDefinitionMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FieldDefinitionMasterRepository extends JpaRepository<FieldDefinitionMaster, Long> {

    List<FieldDefinitionMaster> findByTenantId(String tenantId);

    List<FieldDefinitionMaster> findByTenantIdAndStatus(String tenantId, String status);

    List<FieldDefinitionMaster> findByTenantIdAndFieldCategory(String tenantId, String fieldCategory);

    List<FieldDefinitionMaster> findByTenantIdAndIsSystemField(String tenantId, Boolean isSystemField);

    List<FieldDefinitionMaster> findByTenantIdAndIsCustomField(String tenantId, Boolean isCustomField);

    Optional<FieldDefinitionMaster> findByTenantIdAndFieldName(String tenantId, String fieldName);

    boolean existsByTenantIdAndFieldName(String tenantId, String fieldName);
}
