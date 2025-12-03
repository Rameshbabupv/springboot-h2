package com.hrms.repository;

import com.hrms.entity.HrmsModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for HrmsModule entity
 */
@Repository
public interface HrmsModuleRepository extends JpaRepository<HrmsModule, Long> {

    // Find all active modules by tenant
    List<HrmsModule> findByTenantIdAndIsActiveTrueOrderByDisplayOrder(String tenantId);

    // Find modules by category
    List<HrmsModule> findByTenantIdAndModuleCategoryAndIsActiveTrueOrderByDisplayOrder(
        String tenantId, String moduleCategory);

    // Find by module code
    Optional<HrmsModule> findByTenantIdAndModuleCode(String tenantId, String moduleCode);

    // Find all modules (including inactive)
    List<HrmsModule> findByTenantIdOrderByDisplayOrder(String tenantId);

    // Find child modules
    List<HrmsModule> findByTenantIdAndParentModuleCodeAndIsActiveTrueOrderByDisplayOrder(
        String tenantId, String parentModuleCode);
}
