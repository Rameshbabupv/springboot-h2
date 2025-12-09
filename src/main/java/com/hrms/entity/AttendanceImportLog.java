package com.hrms.entity;

import com.hrms.enums.ImportStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AttendanceImportLog - Tracks history of Excel/CSV attendance imports.
 * Used for audit trail and review purposes.
 */
@Entity
@Table(name = "attendance_import_log",
    indexes = {
        @Index(name = "idx_import_log_tenant_company", columnList = "tenant_id, company_id"),
        @Index(name = "idx_import_log_created_at", columnList = "created_at DESC"),
        @Index(name = "idx_import_log_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceImportLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Import details
    @NotBlank
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @NotNull
    @Column(name = "total_rows", nullable = false)
    private Integer totalRows;

    @Column(name = "imported_count", nullable = false)
    private Integer importedCount = 0;

    @Column(name = "skipped_count", nullable = false)
    private Integer skippedCount = 0;

    @Column(name = "error_count", nullable = false)
    private Integer errorCount = 0;

    // Status
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ImportStatus status = ImportStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // Timing
    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    // Audit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imported_by")
    private UserAccount importedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // Related errors
    @OneToMany(mappedBy = "importLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttendanceImportError> errors = new ArrayList<>();

    /**
     * Helper to add an error.
     */
    public void addError(AttendanceImportError error) {
        errors.add(error);
        error.setImportLog(this);
    }

    /**
     * Mark import as started.
     */
    public void markStarted() {
        this.status = ImportStatus.PROCESSING;
        this.startedAt = OffsetDateTime.now();
    }

    /**
     * Mark import as completed with results.
     */
    public void markCompleted(int imported, int skipped, int errorCount) {
        this.importedCount = imported;
        this.skippedCount = skipped;
        this.errorCount = errorCount;
        this.completedAt = OffsetDateTime.now();

        if (this.startedAt != null) {
            this.processingTimeMs = java.time.Duration.between(startedAt, completedAt).toMillis();
        }

        // Determine final status
        if (errorCount == 0 && imported == totalRows) {
            this.status = ImportStatus.SUCCESS;
        } else if (imported == 0) {
            this.status = ImportStatus.FAILED;
        } else {
            this.status = ImportStatus.PARTIAL;
        }
    }

    /**
     * Mark import as failed.
     */
    public void markFailed(String errorMessage) {
        this.status = ImportStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = OffsetDateTime.now();
        if (this.startedAt != null) {
            this.processingTimeMs = java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }
}
