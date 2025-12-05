package com.hrms.service.impl;

import com.hrms.entity.Company;
import com.hrms.entity.Employee;
import com.hrms.entity.PunchLog;
import com.hrms.enums.PunchSource;
import com.hrms.enums.PunchStatus;
import com.hrms.graphql.input.PunchLogInput;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.PunchLogRepository;
import com.hrms.service.PunchLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation of PunchLogService.
 */
@Service
@Transactional
public class PunchLogServiceImpl implements PunchLogService {

    private final PunchLogRepository punchLogRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    public PunchLogServiceImpl(PunchLogRepository punchLogRepository,
                              EmployeeRepository employeeRepository,
                              CompanyRepository companyRepository) {
        this.punchLogRepository = punchLogRepository;
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public PunchLog recordPunch(String tenantId, Long companyId, PunchLogInput input) {
        PunchLog punchLog = new PunchLog();
        punchLog.setTenantId(tenantId);

        if (companyId != null) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
            punchLog.setCompany(company);
        }

        // Auto-match employee by biometricId if employeeId not provided
        if (input.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(input.getEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + input.getEmployeeId()));
            punchLog.setEmployee(employee);
            punchLog.setStatus(PunchStatus.MATCHED);
        } else if (input.getBiometricId() != null) {
            Long employeeId = findEmployeeByBiometricId(tenantId, input.getBiometricId());
            if (employeeId != null) {
                Employee employee = employeeRepository.findById(employeeId).orElse(null);
                punchLog.setEmployee(employee);
                punchLog.setStatus(PunchStatus.MATCHED);
            } else {
                punchLog.setStatus(PunchStatus.UNMATCHED);
            }
        } else {
            punchLog.setStatus(PunchStatus.UNMATCHED);
        }

        punchLog.setBiometricId(input.getBiometricId());
        punchLog.setDeviceId(input.getDeviceId());
        punchLog.setDeviceName(input.getDeviceName());
        punchLog.setLocation(input.getLocation());
        punchLog.setPunchTime(parseDateTime(input.getPunchTime()));
        punchLog.setPunchType(input.getPunchType());
        punchLog.setVerifyMode(input.getVerifyMode());
        punchLog.setPunchSource(input.getPunchSource());

        return punchLogRepository.save(punchLog);
    }

    @Override
    public int bulkImportPunches(String tenantId, Long companyId, List<PunchLogInput> inputs) {
        int count = 0;
        for (PunchLogInput input : inputs) {
            try {
                recordPunch(tenantId, companyId, input);
                count++;
            } catch (Exception e) {
                // Log and continue with next record
            }
        }
        return count;
    }

    @Override
    public PunchLog mapUnmatchedPunch(String tenantId, Long punchLogId, Long employeeId) {
        PunchLog punchLog = punchLogRepository.findById(punchLogId)
                .orElseThrow(() -> new IllegalArgumentException("PunchLog not found: " + punchLogId));

        if (!punchLog.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Tenant mismatch");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        punchLog.setEmployee(employee);
        punchLog.setStatus(PunchStatus.MATCHED);

        return punchLogRepository.save(punchLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PunchLog> getPunchLogs(String tenantId, Long companyId,
                                        OffsetDateTime dateFrom, OffsetDateTime dateTo,
                                        Long employeeId, PunchStatus status) {
        if (employeeId != null) {
            return punchLogRepository.findByEmployeeAndTimeRange(employeeId, dateFrom, dateTo);
        }
        return punchLogRepository.findByTenantAndTimeRange(tenantId, dateFrom, dateTo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PunchLog> getEmployeePunches(Long employeeId, OffsetDateTime startTime, OffsetDateTime endTime) {
        return punchLogRepository.findByEmployeeAndTimeRange(employeeId, startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PunchLog> getUnmatchedPunches(String tenantId, OffsetDateTime startTime, OffsetDateTime endTime) {
        return punchLogRepository.findUnmatchedByTenantAndTimeRange(tenantId, startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public Long findEmployeeByBiometricId(String tenantId, String biometricId) {
        // TODO: Implement lookup in employee table by biometric_id field
        // For now, return null (punch will be marked as UNMATCHED)
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public long getPunchCount(Long employeeId, OffsetDateTime startTime, OffsetDateTime endTime) {
        return punchLogRepository.countByEmployeeAndTimeRange(employeeId, startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDuplicatePunch(Long employeeId, OffsetDateTime punchTime, String source) {
        return punchLogRepository.existsDuplicatePunch(employeeId, punchTime, PunchSource.valueOf(source));
    }

    private OffsetDateTime parseDateTime(String dateTime) {
        return OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
    }
}
