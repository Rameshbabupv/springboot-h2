package com.hrms.entity;

import com.hrms.enums.PunchSource;
import com.hrms.enums.PunchStatus;
import com.hrms.enums.PunchType;
import com.hrms.enums.VerifyMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * PunchLog - Raw biometric/portal punch data.
 * Stores all punch events before processing into DailyAttendance.
 */
@Entity
@Table(name = "punch_logs",
    indexes = {
        @Index(name = "idx_punch_logs_tenant", columnList = "tenant_id"),
        @Index(name = "idx_punch_logs_tenant_date", columnList = "tenant_id, punch_time"),
        @Index(name = "idx_punch_logs_employee", columnList = "employee_id, punch_time"),
        @Index(name = "idx_punch_logs_biometric", columnList = "biometric_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PunchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "biometric_id", length = 50)
    private String biometricId;

    @Column(name = "device_id", length = 50)
    private String deviceId;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "location", length = 200)
    private String location;

    @NotNull
    @Column(name = "punch_time", nullable = false)
    private OffsetDateTime punchTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "punch_type", nullable = false, length = 5)
    private PunchType punchType;

    @Enumerated(EnumType.STRING)
    @Column(name = "verify_mode", length = 20)
    private VerifyMode verifyMode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "punch_source", nullable = false, length = 20)
    private PunchSource punchSource;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PunchStatus status;

    /**
     * Raw data from biometric device or import.
     * Stores original data for audit/debugging.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data", columnDefinition = "jsonb")
    private Map<String, Object> rawData;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    // Constructor for quick creation
    public PunchLog(String tenantId, Employee employee, OffsetDateTime punchTime,
                    PunchType punchType, PunchSource punchSource) {
        this.tenantId = tenantId;
        this.employee = employee;
        this.punchTime = punchTime;
        this.punchType = punchType;
        this.punchSource = punchSource;
        this.status = PunchStatus.UNMATCHED;
    }
}
