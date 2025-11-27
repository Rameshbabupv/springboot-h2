package com.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "employee_template_field", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"template_id", "section_id", "field_id"})
}, indexes = {
    @Index(name = "idx_template_field_template", columnList = "template_id"),
    @Index(name = "idx_template_field_section", columnList = "section_id"),
    @Index(name = "idx_template_field_order", columnList = "section_id, display_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTemplateField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private EmployeeTemplate template;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private EmployeeTemplateSection section;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private FieldDefinitionMaster field;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "display_width", length = 20)
    private String displayWidth = "full";

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Column(name = "is_readonly", nullable = false)
    private Boolean isReadonly = false;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;

    @Column(name = "is_editable", nullable = false)
    private Boolean isEditable = true;

    @Column(name = "label_override", length = 200)
    private String labelOverride;

    @Column(name = "help_text_override", columnDefinition = "TEXT")
    private String helpTextOverride;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_override", columnDefinition = "jsonb")
    private Map<String, Object> validationOverride;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditional_logic", columnDefinition = "jsonb")
    private Map<String, Object> conditionalLogic;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
