package com.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "employee_template_version", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"template_id", "version"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTemplateVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private EmployeeTemplate template;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String version;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "template_snapshot", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> templateSnapshot;

    @Column(name = "change_summary", columnDefinition = "TEXT")
    private String changeSummary;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
