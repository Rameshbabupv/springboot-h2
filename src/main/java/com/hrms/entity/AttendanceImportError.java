package com.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AttendanceImportError - Detailed error tracking for attendance imports.
 * Each record represents a single row error during import.
 */
@Entity
@Table(name = "attendance_import_error",
    indexes = {
        @Index(name = "idx_import_error_log", columnList = "import_log_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceImportError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_log_id", nullable = false)
    private AttendanceImportLog importLog;

    @NotNull
    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;

    @Column(name = "employee_id", length = 50)
    private String employeeId;

    @Column(name = "column_name", length = 50)
    private String columnName;

    @NotBlank
    @Column(name = "error_type", nullable = false, length = 50)
    private String errorType;

    @NotBlank
    @Column(name = "error_message", nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Constructor for quick creation.
     */
    public AttendanceImportError(Integer rowNumber, String employeeId, String columnName,
                                  String errorType, String errorMessage) {
        this.rowNumber = rowNumber;
        this.employeeId = employeeId;
        this.columnName = columnName;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }
}
