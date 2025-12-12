package com.hrms.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.config.SupabaseConfig;
import com.hrms.entity.Company;
import com.hrms.entity.Employee;
import com.hrms.entity.PunchLog;
import com.hrms.enums.PunchSource;
import com.hrms.enums.PunchStatus;
import com.hrms.enums.PunchType;
import com.hrms.enums.VerifyMode;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.PunchLogRepository;
import com.hrms.service.BiometricSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implementation of BiometricSyncService.
 * Fetches biometric punch data from Supabase and imports into HRMS punch_logs table.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BiometricSyncServiceImpl implements BiometricSyncService {

    private final SupabaseConfig supabaseConfig;
    private final RestTemplate restTemplate;
    private final PunchLogRepository punchLogRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Tenant/Company mapping from Supabase (001 -> TENANT001)
    private static final Map<String, String> TENANT_MAPPING = Map.of(
        "001", "TENANT001"
    );

    @Override
    public boolean testSupabaseConnection() {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                supabaseConfig.getBiometricDataEndpoint() + "?select=count&limit=1",
                HttpMethod.GET,
                entity,
                String.class
            );

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Supabase connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int getSupabaseRecordCount() {
        try {
            HttpHeaders headers = createHeaders();
            headers.set("Prefer", "count=exact");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                supabaseConfig.getBiometricDataEndpoint() + "?select=count",
                HttpMethod.GET,
                entity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray() && root.size() > 0) {
                    return root.get(0).get("count").asInt();
                }
            }
            return 0;
        } catch (Exception e) {
            log.error("Failed to get Supabase record count: {}", e.getMessage());
            return -1;
        }
    }

    @Override
    @Transactional
    public BiometricSyncResult syncFromSupabase(String tenantId, Long companyId) {
        return syncFromSupabase(tenantId, companyId, null, null);
    }

    /**
     * Sync biometric data from Supabase to HRMS (ADR-002 implementation).
     * Features:
     * - Incremental fetch (only sync_status=NULL)
     * - SHA256-based primary deduplication (O(1) lookup)
     * - Time-window secondary deduplication (for HR_MANUAL fallback)
     * - Batch processing (100 records per transaction)
     * - Bi-directional write-back (eventual consistency)
     *
     * @param tenantId Tenant ID
     * @param companyId Company ID
     * @param fromDate Start date filter
     * @param toDate End date filter
     * @return Sync result with counts
     */
    @Override
    public BiometricSyncResult syncFromSupabase(String tenantId, Long companyId, LocalDate fromDate, LocalDate toDate) {
        log.info("Starting biometric sync for tenantId={}, companyId={}", tenantId, companyId);

        int totalFetched = 0;
        int inserted = 0;
        int skippedDuplicate = 0;
        int skippedNoEmployee = 0;
        int failed = 0;

        try {
            // STEP 1: Fetch unprocessed records from Supabase (incremental sync)
            List<Map<String, Object>> records = fetchFromSupabase();
            totalFetched = records.size();

            if (records.isEmpty()) {
                log.info("No new biometric records to sync");
                return new BiometricSyncResult(0, 0, 0, 0, 0, "No new records to sync");
            }

            log.info("Fetched {} records from Supabase", totalFetched);

            // STEP 2: Build employee lookup cache (one-time per sync)
            Map<String, Employee> employeeCache = buildEmployeeCache(tenantId);
            log.debug("Built employee cache with {} entries", employeeCache.size());

            // STEP 3: Process records in batches
            final int BATCH_SIZE = 100;
            List<PunchLog> currentBatch = new ArrayList<>();
            List<String> idsToMarkSynced = new ArrayList<>();
            List<String> idsToMarkDuplicate = new ArrayList<>();

            for (Map<String, Object> record : records) {
                try {
                    String supabaseId = (String) record.get("id"); // SHA256 from Supabase
                    String deviceData = (String) record.get("device_data");

                    if (supabaseId == null || deviceData == null) {
                        log.warn("Missing required fields (id or device_data) in record: {}", record);
                        failed++;
                        continue;
                    }

                    // ADR-002: PRIMARY DEDUPLICATION - Check SHA256 first (O(1))
                    if (punchLogRepository.existsBySha256(supabaseId)) {
                        log.debug("Duplicate detected via SHA256: {}", supabaseId);
                        skippedDuplicate++;
                        idsToMarkDuplicate.add(supabaseId);
                        continue;
                    }

                    // STEP 4: Parse device_data CSV
                    String[] parts = deviceData.split(",");
                    if (parts.length < 8) {
                        log.warn("Invalid device_data format (expected 8 fields): {}", deviceData);
                        failed++;
                        continue;
                    }

                    String biometricId = parts[4].trim(); // userId from CSV
                    String logDateStr = parts[5].trim();
                    String direction = parts[6].trim(); // IN/OUT

                    OffsetDateTime punchTime = parsePunchTime(logDateStr);
                    if (punchTime == null) {
                        log.warn("Failed to parse punch time: {}", logDateStr);
                        failed++;
                        continue;
                    }

                    // Filter by date range if specified
                    if (fromDate != null && punchTime.toLocalDate().isBefore(fromDate)) {
                        continue;
                    }
                    if (toDate != null && punchTime.toLocalDate().isAfter(toDate)) {
                        continue;
                    }

                    // STEP 5: Match employee by biometricId
                    Employee employee = employeeCache.get(biometricId);
                    if (employee == null) {
                        log.warn("No employee found for biometricId: {}", biometricId);
                        skippedNoEmployee++;
                        continue;
                    }

                    // STEP 6: SECONDARY DEDUPLICATION - Time window check (for HR_MANUAL fallback)
                    // Note: This is redundant for SUPABASE source (already checked SHA256)
                    // but kept for consistency with manual entry path
                    if (isDuplicatePunch(employee.getId(), punchTime, supabaseId)) {
                        log.debug("Duplicate detected via time window: employeeId={}, time={}",
                                 employee.getId(), punchTime);
                        skippedDuplicate++;
                        idsToMarkDuplicate.add(supabaseId);
                        continue;
                    }

                    // STEP 7: Create PunchLog entity
                    PunchLog punch = new PunchLog();
                    punch.setTenantId(tenantId);
                    punch.setEmployee(employee);
                    punch.setPunchTime(punchTime);
                    punch.setPunchType(determinePunchType(direction, punchTime, employee.getId()));
                    punch.setPunchSource(PunchSource.SUPABASE); // ADR-002: Specific source
                    punch.setSha256(supabaseId); // ADR-002: Store SHA256 from Supabase
                    punch.setStatus(PunchStatus.MATCHED);
                    punch.setVerifyMode(VerifyMode.FINGER);
                    punch.setBiometricId(biometricId);
                    punch.setDeviceId(parts[7].trim());
                    punch.setDeviceName("ESSL Device " + parts[7].trim());
                    punch.setLocation(employee.getLocation() != null ? employee.getLocation().getName() : null);
                    
                    Map<String, Object> rawData = new HashMap<>();
                    rawData.put("id", supabaseId);
                    rawData.put("deviceData", deviceData);
                    punch.setRawData(rawData);

                    // Add to current batch
                    currentBatch.add(punch);

                    // STEP 8: Process batch when full
                    if (currentBatch.size() >= BATCH_SIZE) {
                        List<String> batchSynced = new ArrayList<>();
                        List<String> batchDuplicates = new ArrayList<>();
                        processBatch(currentBatch, batchSynced, batchDuplicates);
                        inserted += batchSynced.size();
                        idsToMarkSynced.addAll(batchSynced);
                        idsToMarkDuplicate.addAll(batchDuplicates);
                        currentBatch.clear();
                    }

                } catch (Exception e) {
                    log.error("Error processing record: {}", record, e);
                    failed++;
                }
            }

            // STEP 9: Process remaining records in final batch
            if (!currentBatch.isEmpty()) {
                List<String> batchSynced = new ArrayList<>();
                List<String> batchDuplicates = new ArrayList<>();
                processBatch(currentBatch, batchSynced, batchDuplicates);
                inserted += batchSynced.size();
                idsToMarkSynced.addAll(batchSynced);
                idsToMarkDuplicate.addAll(batchDuplicates);
            }

            // STEP 10: Write-back to Supabase (eventual consistency)
            updateSupabaseStatusBatch(idsToMarkSynced, "synced");
            updateSupabaseStatusBatch(idsToMarkDuplicate, "reject-duplicate");

            String message = String.format(
                "Sync completed: %d fetched, %d inserted, %d duplicate, %d no-employee, %d failed",
                totalFetched, inserted, skippedDuplicate, skippedNoEmployee, failed
            );
            log.info(message);

            return new BiometricSyncResult(
                totalFetched, inserted, skippedDuplicate, skippedNoEmployee, failed, message
            );

        } catch (Exception e) {
            String errorMsg = "Biometric sync failed: " + e.getMessage();
            log.error(errorMsg, e);
            return new BiometricSyncResult(
                totalFetched, inserted, skippedDuplicate, skippedNoEmployee, failed, errorMsg
            );
        }
    }

    /**
     * Fetch unprocessed biometric records from Supabase (incremental sync per ADR-002).
     * Only fetches records where sync_status IS NULL (not yet processed).
     *
     * @return List of unprocessed records (max 1000)
     */
    private List<Map<String, Object>> fetchFromSupabase() {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // ADR-002: Incremental sync - only unprocessed records
            String url = supabaseConfig.getBiometricDataEndpoint()
                       + "?select=*"
                       + "&sync_status=is.null"           // NEW: Only NULL status
                       + "&order=created_at.asc"          // CHANGED: FIFO processing (was .desc)
                       + "&limit=1000";                   // NEW: Batch limit

            log.debug("Fetching from Supabase: {}", url);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                List<Map<String, Object>> records = new ArrayList<>();

                for (JsonNode node : root) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", node.has("id") ? node.get("id").asText() : null);  // SHA256
                    record.put("device_data", node.has("device_data") ? node.get("device_data").asText() : null);
                    record.put("created_at", node.has("created_at") ? node.get("created_at").asText() : null);
                    record.put("sync_status", node.has("sync_status") ? node.get("sync_status").asText() : null);
                    records.add(record);
                }

                log.info("Fetched {} unprocessed records from Supabase (incremental sync)", records.size());
                return records;
            }

            log.warn("Unexpected response from Supabase: {}", response.getStatusCode());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch from Supabase: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Update sync status in Supabase (write-back per ADR-002).
     * Uses eventual consistency model - failures are logged but not thrown.
     *
     * @param supabaseId SHA256 ID from Supabase
     * @param status 'synced' or 'reject-duplicate'
     */
    private void updateSupabaseStatus(String supabaseId, String status) {
        try {
            String url = supabaseConfig.getBiometricDataEndpoint()
                       + "?id=eq." + supabaseId;

            HttpHeaders headers = createHeaders();
            headers.set("Prefer", "return=minimal"); // Don't return response body (performance)

            Map<String, Object> body = Map.of(
                "sync_status", status,
                "synced_at", OffsetDateTime.now().toString()
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.PATCH, entity, String.class
            );

            if (response.getStatusCode() == HttpStatus.OK ||
                response.getStatusCode() == HttpStatus.NO_CONTENT) {
                log.debug("Updated Supabase record {} to status: {}", supabaseId, status);
            } else {
                log.warn("Unexpected PATCH response from Supabase: {} for ID: {}",
                         response.getStatusCode(), supabaseId);
            }

        } catch (Exception e) {
            // EVENTUAL CONSISTENCY: Log warning, don't throw
            // Punch is already inserted in HRMS - sync status is secondary
            // Next sync will re-fetch → duplicate check catches it → marks 'reject-duplicate'
            log.warn("Failed to update Supabase status for {} to '{}': {}. Continuing anyway.",
                     supabaseId, status, e.getMessage());
        }
    }

    /**
     * Batch update Supabase status for multiple records.
     *
     * @param supabaseIds List of SHA256 IDs
     * @param status Status to set
     */
    private void updateSupabaseStatusBatch(List<String> supabaseIds, String status) {
        if (supabaseIds == null || supabaseIds.isEmpty()) {
            return;
        }

        log.info("Updating {} Supabase records to status: {}", supabaseIds.size(), status);

        for (String id : supabaseIds) {
            updateSupabaseStatus(id, status);
        }

        // Note: Supabase REST API doesn't support bulk PATCH in single request
        // Future optimization: Use Supabase RPC function for bulk updates
    }

    /**
     * Process a batch of PunchLogs atomically (ADR-002).
     * Handles DataIntegrityViolationException for race conditions.
     *
     * @param batch List of PunchLog entities to insert
     * @param idsToMarkSynced List to populate with successfully synced IDs
     * @param idsToMarkDuplicate List to populate with duplicate IDs
     */
    @Transactional
    private void processBatch(List<PunchLog> batch,
                             List<String> idsToMarkSynced,
                             List<String> idsToMarkDuplicate) {
        for (PunchLog punch : batch) {
            try {
                punchLogRepository.save(punch);
                idsToMarkSynced.add(punch.getSha256());
                log.debug("Inserted punch: employeeId={}, sha256={}", punch.getEmployee().getId(), punch.getSha256());
            } catch (DataIntegrityViolationException e) {
                // Race condition: Another thread inserted between check and save
                log.warn("Constraint violation for SHA256 {}: {}. Marking as duplicate.",
                         punch.getSha256(), e.getMessage());
                idsToMarkDuplicate.add(punch.getSha256());
            } catch (Exception e) {
                log.error("Unexpected error processing punch with SHA256 {}: {}",
                         punch.getSha256(), e.getMessage(), e);
                idsToMarkDuplicate.add(punch.getSha256());
            }
        }

        log.info("Batch processed: {} inserted, {} duplicates",
                 idsToMarkSynced.size(), idsToMarkDuplicate.size());
    }

    /**
     * Build in-memory cache of employee biometricId -> Employee.
     * Reduces DB queries from O(n) to O(1) per sync.
     *
     * @param tenantId Tenant ID
     * @return Map of biometricId to Employee
     */
    private Map<String, Employee> buildEmployeeCache(String tenantId) {
        List<Employee> employees = employeeRepository.findByTenantId(tenantId);
        Map<String, Employee> cache = new HashMap<>();

        for (Employee emp : employees) {
            if (emp.getBiometricId() != null && !emp.getBiometricId().isBlank()) {
                cache.put(emp.getBiometricId().trim(), emp);
            }
        }

        return cache;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseConfig.getSupabaseKey());
        headers.set("Authorization", "Bearer " + supabaseConfig.getSupabaseKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private OffsetDateTime parsePunchTime(String dateTimeStr) {
        try {
            // Format: 2025-12-05 23:19:13
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
            return localDateTime.atOffset(ZoneOffset.ofHoursMinutes(5, 30)); // IST
        } catch (Exception e) {
            log.error("Failed to parse date: {}", dateTimeStr);
            return null;
        }
    }

    private Employee findEmployeeByBiometricId(String tenantId, String biometricId) {
        // First try to find by biometricId field
        List<Employee> employees = employeeRepository.findByTenantIdAndBiometricId(tenantId, biometricId);
        if (!employees.isEmpty()) {
            return employees.get(0);
        }

        // If not found, try empId
        Optional<Employee> byEmpId = employeeRepository.findByTenantIdAndEmpId(tenantId, biometricId);
        return byEmpId.orElse(null);
    }

    private boolean isDuplicatePunch(Long employeeId, OffsetDateTime punchTime, String supabaseId) {
        // Check if punch already exists within 1 minute
        OffsetDateTime start = punchTime.minusMinutes(1);
        OffsetDateTime end = punchTime.plusMinutes(1);

        List<PunchLog> existing = punchLogRepository.findByEmployeeIdAndPunchTimeBetween(employeeId, start, end);
        return !existing.isEmpty();
    }

    private PunchType determinePunchType(String direction, OffsetDateTime punchTime, Long employeeId) {
        // If direction is specified, use it
        if ("IN".equalsIgnoreCase(direction)) {
            return PunchType.IN;
        }
        if ("OUT".equalsIgnoreCase(direction)) {
            return PunchType.OUT;
        }

        // Otherwise, alternate based on existing punches for the day
        LocalDate punchDate = punchTime.toLocalDate();
        OffsetDateTime dayStart = punchDate.atStartOfDay().atOffset(ZoneOffset.ofHoursMinutes(5, 30));
        OffsetDateTime dayEnd = punchDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.ofHoursMinutes(5, 30));

        long punchCount = punchLogRepository.countByEmployeeIdAndPunchTimeBetween(employeeId, dayStart, dayEnd);

        // Even count = IN, Odd count = OUT
        return punchCount % 2 == 0 ? PunchType.IN : PunchType.OUT;
    }
}
