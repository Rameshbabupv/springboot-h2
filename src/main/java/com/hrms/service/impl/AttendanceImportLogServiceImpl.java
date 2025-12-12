package com.hrms.service.impl;

import com.hrms.entity.AttendanceImportError;
import com.hrms.entity.AttendanceImportLog;
import com.hrms.entity.Company;
import com.hrms.entity.UserAccount;
import com.hrms.enums.ImportStatus;
import com.hrms.repository.AttendanceImportErrorRepository;
import com.hrms.repository.AttendanceImportLogRepository;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.UserAccountRepository;
import com.hrms.service.AttendanceImportLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of AttendanceImportLogService.
 */
@Slf4j
@Service
@Transactional
public class AttendanceImportLogServiceImpl implements AttendanceImportLogService {

    private final AttendanceImportLogRepository importLogRepository;
    private final AttendanceImportErrorRepository importErrorRepository;
    private final CompanyRepository companyRepository;
    private final UserAccountRepository userAccountRepository;

    public AttendanceImportLogServiceImpl(
            AttendanceImportLogRepository importLogRepository,
            AttendanceImportErrorRepository importErrorRepository,
            CompanyRepository companyRepository,
            UserAccountRepository userAccountRepository) {
        this.importLogRepository = importLogRepository;
        this.importErrorRepository = importErrorRepository;
        this.companyRepository = companyRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public AttendanceImportLog createImportLog(String tenantId, Long companyId, String fileName,
                                                Long fileSizeBytes, int totalRows, Long importedByUserId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        AttendanceImportLog importLog = new AttendanceImportLog();
        importLog.setTenantId(tenantId);
        importLog.setCompany(company);
        importLog.setFileName(fileName);
        importLog.setFileSizeBytes(fileSizeBytes);
        importLog.setTotalRows(totalRows);

        if (importedByUserId != null) {
            userAccountRepository.findById(importedByUserId)
                    .ifPresent(importLog::setImportedBy);
        }

        importLog.markStarted();

        log.info("Created import log for file: {} with {} rows", fileName, totalRows);
        return importLogRepository.save(importLog);
    }

    @Override
    public AttendanceImportLog updateImportResults(Long importLogId, int importedCount,
                                                    int skippedCount, List<AttendanceImportError> errors) {
        AttendanceImportLog importLog = importLogRepository.findById(importLogId)
                .orElseThrow(() -> new IllegalArgumentException("Import log not found: " + importLogId));

        // Add errors to the log
        if (errors != null && !errors.isEmpty()) {
            for (AttendanceImportError error : errors) {
                importLog.addError(error);
            }
        }

        importLog.markCompleted(importedCount, skippedCount, errors != null ? errors.size() : 0);

        log.info("Import {} completed: {} imported, {} skipped, {} errors",
                importLogId, importedCount, skippedCount, errors != null ? errors.size() : 0);

        return importLogRepository.save(importLog);
    }

    @Override
    public AttendanceImportLog markImportFailed(Long importLogId, String errorMessage) {
        AttendanceImportLog importLog = importLogRepository.findById(importLogId)
                .orElseThrow(() -> new IllegalArgumentException("Import log not found: " + importLogId));

        importLog.markFailed(errorMessage);

        log.error("Import {} failed: {}", importLogId, errorMessage);
        return importLogRepository.save(importLog);
    }

    @Override
    @Transactional(readOnly = true)
    public ImportLogPage getImportHistory(String tenantId, Long companyId, int limit, int offset,
                                           ImportStatus status, OffsetDateTime fromDate, OffsetDateTime toDate) {
        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit);

        // Convert enum to String for native query (null-safe)
        String statusString = status != null ? status.name() : null;

        Page<AttendanceImportLog> resultPage = importLogRepository.findWithFilters(
                tenantId, companyId, statusString, fromDate, toDate, pageRequest);

        int totalPages = resultPage.getTotalPages();
        int currentPage = page + 1;

        PageInfo pageInfo = new PageInfo(
                resultPage.hasNext(),
                resultPage.hasPrevious(),
                currentPage,
                totalPages
        );

        return new ImportLogPage(resultPage.getContent(), resultPage.getTotalElements(), pageInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AttendanceImportLog> getImportDetails(String tenantId, Long importId) {
        return importLogRepository.findByIdAndTenant(importId, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceImportError> getImportErrors(String tenantId, Long importId, int limit, int offset) {
        // Verify tenant access first
        Optional<AttendanceImportLog> importLog = importLogRepository.findByIdAndTenant(importId, tenantId);
        if (importLog.isEmpty()) {
            throw new IllegalArgumentException("Import log not found or access denied: " + importId);
        }

        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit);

        return importErrorRepository.findByImportLogId(importId, pageRequest).getContent();
    }
}
