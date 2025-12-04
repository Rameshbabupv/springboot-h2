package com.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Leave Application Attachment Entity
 * Attachments for leave applications (medical certificates, etc.).
 *
 * TRANSACTIONAL TABLE - company_id is always required (denormalized from parent).
 */
@Entity
@Table(name = "leave_application_attachments",
    indexes = {
        @Index(name = "idx_leave_attachment_tenant", columnList = "tenant_id"),
        @Index(name = "idx_leave_attachment_application", columnList = "leave_application_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApplicationAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Denormalized for RLS/filtering.
     */
    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    /**
     * Denormalized from parent application.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_application_id", nullable = false)
    private LeaveApplication leaveApplication;

    @NotBlank
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @NotBlank
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @NotBlank
    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;  // MIME type

    @NotNull
    @Column(name = "file_size", nullable = false)
    private Integer fileSize;  // Size in bytes

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private OffsetDateTime uploadedAt;
}
