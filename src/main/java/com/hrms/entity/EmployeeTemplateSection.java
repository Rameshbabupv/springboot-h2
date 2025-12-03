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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "employee_template_section", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"template_id", "section_code"})
}, indexes = {
    @Index(name = "idx_section_template", columnList = "template_id"),
    @Index(name = "idx_section_order", columnList = "template_id, section_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTemplateSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private EmployeeTemplate template;

    @NotBlank
    @Column(name = "section_name", nullable = false, length = 200)
    private String sectionName;

    @NotBlank
    @Column(name = "section_code", nullable = false, length = 100)
    private String sectionCode;

    @Column(name = "section_description", columnDefinition = "TEXT")
    private String sectionDescription;

    @Column(name = "section_order", nullable = false)
    private Integer sectionOrder;

    @Column(name = "section_icon", length = 50)
    private String sectionIcon;

    @Column(name = "is_collapsible", nullable = false)
    private Boolean isCollapsible = true;

    @Column(name = "is_expanded_by_default", nullable = false)
    private Boolean isExpandedByDefault = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditional_logic", columnDefinition = "jsonb")
    private Map<String, Object> conditionalLogic;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<EmployeeTemplateField> fields = new ArrayList<>();

    // Helper methods
    public void addField(EmployeeTemplateField field) {
        fields.add(field);
        field.setSection(this);
    }

    public void removeField(EmployeeTemplateField field) {
        fields.remove(field);
        field.setSection(null);
    }
}
