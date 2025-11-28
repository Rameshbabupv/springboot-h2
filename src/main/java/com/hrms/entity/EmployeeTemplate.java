package com.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employee_template", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "template_code"})
}, indexes = {
    @Index(name = "idx_template_tenant", columnList = "tenant_id"),
    @Index(name = "idx_template_active", columnList = "is_active"),
    @Index(name = "idx_template_effective", columnList = "effective_from, effective_to")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @NotBlank
    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;

    @NotBlank
    @Column(name = "template_code", nullable = false, length = 100)
    private String templateCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "change_notes", columnDefinition = "TEXT")
    private String changeNotes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_categories", columnDefinition = "jsonb")
    private List<String> applicableCategories = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_groups", columnDefinition = "jsonb")
    private List<String> applicableGroups = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_grades", columnDefinition = "jsonb")
    private List<String> applicableGrades = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_companies", columnDefinition = "jsonb")
    private List<Long> applicableCompanies = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_locations", columnDefinition = "jsonb")
    private List<Long> applicableLocations = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_divisions", columnDefinition = "jsonb")
    private List<String> applicableDivisions = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_departments", columnDefinition = "jsonb")
    private List<String> applicableDepartments = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_sections", columnDefinition = "jsonb")
    private List<String> applicableSections = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_designations", columnDefinition = "jsonb")
    private List<String> applicableDesignations = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_job_functions", columnDefinition = "jsonb")
    private List<String> applicableJobFunctions = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_employment_types", columnDefinition = "jsonb")
    private List<String> applicableEmploymentTypes = new ArrayList<>();

    // Field Configuration
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "standard_fields", columnDefinition = "jsonb")
    private String standardFields;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_fields", columnDefinition = "jsonb")
    private String customFields;

    // Assignment Criteria - The 9 Organizational Parameters
    // null = applies to all (wildcard), specific ID = applies only to that value
    @Column(name = "company_id")
    private String companyId;

    @Column(name = "location_id")
    private String locationId;

    @Column(name = "division_id")
    private String divisionId;

    @Column(name = "department_id")
    private String departmentId;

    @Column(name = "section_id")
    private String sectionId;

    @Column(name = "designation_id")
    private String designationId;

    @Column(name = "job_function_id")
    private String jobFunctionId;

    @Column(name = "employment_type_id")
    private String employmentTypeId;

    @Column(name = "grade_id")
    private String gradeId;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(length = 20)
    private String version;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<EmployeeTemplateSection> sections = new ArrayList<>();

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<EmployeeTemplateField> fields = new ArrayList<>();

    // Helper methods
    public void addSection(EmployeeTemplateSection section) {
        sections.add(section);
        section.setTemplate(this);
    }

    public void removeSection(EmployeeTemplateSection section) {
        sections.remove(section);
        section.setTemplate(null);
    }

    public void addField(EmployeeTemplateField field) {
        fields.add(field);
        field.setTemplate(this);
    }

    public void removeField(EmployeeTemplateField field) {
        fields.remove(field);
        field.setTemplate(null);
    }
}
