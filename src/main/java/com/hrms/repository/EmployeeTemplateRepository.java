package com.hrms.repository;

import com.hrms.entity.EmployeeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeTemplateRepository extends JpaRepository<EmployeeTemplate, Long> {

    List<EmployeeTemplate> findByTenantIdAndIsActive(String tenantId, Boolean isActive);

    List<EmployeeTemplate> findByTenantId(String tenantId);

    Optional<EmployeeTemplate> findByTenantIdAndTemplateCode(String tenantId, String templateCode);

    Optional<EmployeeTemplate> findByTenantIdAndIsDefaultTrue(String tenantId);

    boolean existsByTenantIdAndTemplateCode(String tenantId, String templateCode);

    @Query(value = """
        SELECT * FROM employee_template t
        WHERE t.tenant_id = :tenantId
        AND t.is_active = true
        AND (t.effective_from IS NULL OR t.effective_from <= CAST(:currentDate AS date))
        AND (t.effective_to IS NULL OR t.effective_to >= CAST(:currentDate AS date))
        AND (
            t.applicable_categories @> to_jsonb(ARRAY[:category])
            OR jsonb_array_length(COALESCE(t.applicable_categories, '[]'::jsonb)) = 0
        )
        ORDER BY t.priority DESC
    """, nativeQuery = true)
    List<EmployeeTemplate> findApplicableTemplates(
        @Param("tenantId") String tenantId,
        @Param("currentDate") String currentDate,
        @Param("category") String category
    );

    @Query(value = """
        SELECT * FROM employee_template t
        WHERE t.tenant_id = :tenantId
        AND t.is_active = true
        AND (t.effective_from IS NULL OR t.effective_from <= CAST(:currentDate AS date))
        AND (t.effective_to IS NULL OR t.effective_to >= CAST(:currentDate AS date))
        AND (
            (:category IS NULL OR t.applicable_categories @> to_jsonb(ARRAY[:category]) OR jsonb_array_length(COALESCE(t.applicable_categories, '[]'::jsonb)) = 0)
            AND (:groupName IS NULL OR t.applicable_groups @> to_jsonb(ARRAY[:groupName]) OR jsonb_array_length(COALESCE(t.applicable_groups, '[]'::jsonb)) = 0)
            AND (:grade IS NULL OR t.applicable_grades @> to_jsonb(ARRAY[:grade]) OR jsonb_array_length(COALESCE(t.applicable_grades, '[]'::jsonb)) = 0)
            AND (:companyId IS NULL OR t.applicable_companies @> to_jsonb(ARRAY[:companyId]) OR jsonb_array_length(COALESCE(t.applicable_companies, '[]'::jsonb)) = 0)
            AND (:locationId IS NULL OR t.applicable_locations @> to_jsonb(ARRAY[:locationId]) OR jsonb_array_length(COALESCE(t.applicable_locations, '[]'::jsonb)) = 0)
        )
        ORDER BY t.priority DESC
    """, nativeQuery = true)
    List<EmployeeTemplate> findApplicableTemplatesByCriteria(
        @Param("tenantId") String tenantId,
        @Param("currentDate") String currentDate,
        @Param("category") String category,
        @Param("groupName") String group,
        @Param("grade") String grade,
        @Param("companyId") String companyId,
        @Param("locationId") String locationId
    );
}
