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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "field_definition_master", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "field_name"})
}, indexes = {
    @Index(name = "idx_field_def_tenant", columnList = "tenant_id"),
    @Index(name = "idx_field_def_category", columnList = "field_category"),
    @Index(name = "idx_field_def_status", columnList = "status"),
    @Index(name = "idx_field_def_system", columnList = "is_system_field")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldDefinitionMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @NotBlank
    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @NotBlank
    @Column(name = "field_label", nullable = false, length = 200)
    private String fieldLabel;

    @Column(name = "field_code", length = 100)
    private String fieldCode;

    @NotBlank
    @Column(name = "field_type", nullable = false, length = 50)
    private String fieldType;

    @Column(name = "field_category", length = 100)
    private String fieldCategory;

    @NotBlank
    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_rules", columnDefinition = "jsonb")
    private Map<String, Object> validationRules;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dropdown_options", columnDefinition = "jsonb")
    private List<Map<String, String>> dropdownOptions = new ArrayList<>();

    @Column(name = "is_system_field", nullable = false)
    private Boolean isSystemField = false;

    @Column(name = "is_custom_field", nullable = false)
    private Boolean isCustomField = false;

    @Column(name = "is_searchable", nullable = false)
    private Boolean isSearchable = true;

    @Column(name = "is_required_by_default", nullable = false)
    private Boolean isRequiredByDefault = false;

    @Column(name = "help_text", columnDefinition = "TEXT")
    private String helpText;

    @Column(name = "placeholder_text", length = 200)
    private String placeholderText;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String status = "active";

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

    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmployeeTemplateField> templateFields = new ArrayList<>();
}
