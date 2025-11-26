package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sections", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenantId", "department_id", "code"}),
    @UniqueConstraint(columnNames = {"tenantId", "department_id", "name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(length = 50)
    private String createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 50)
    private String updatedBy;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
