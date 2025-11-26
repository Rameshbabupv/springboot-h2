package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "grades", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenantId", "code"}),
    @UniqueConstraint(columnNames = {"tenantId", "name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String tenantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(length = 500)
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
