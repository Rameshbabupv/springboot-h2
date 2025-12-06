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

    @Override
    @Transactional
    public BiometricSyncResult syncFromSupabase(String tenantId, Long companyId, LocalDate fromDate, LocalDate toDate) {
        log.info("Starting biometric sync for tenant: {}, company: {}", tenantId, companyId);

        int totalFetched = 0;
        int inserted = 0;
        int skippedDuplicate = 0;
        int skippedNoEmployee = 0;
        int failed = 0;

        try {
            // Fetch data from Supabase
            List<Map<String, Object>> supabaseRecords = fetchFromSupabase();
            totalFetched = supabaseRecords.size();

            log.info("Fetched {} records from Supabase", totalFetched);

            // Get company
            Company company = null;
            if (companyId != null) {
                company = companyRepository.findById(companyId).orElse(null);
            }

            // Cache for employee lookups by biometric ID
            Map<String, Employee> employeeCache = new HashMap<>();

            for (Map<String, Object> record : supabaseRecords) {
                try {
                    // Parse the device_data CSV
                    String deviceData = (String) record.get("device_data");
                    String supabaseId = (String) record.get("id");

                    if (deviceData == null) {
                        failed++;
                        continue;
                    }

                    // Parse CSV: tenant_id,company_id,location_id,DeviceLogId,UserId,LogDate,Direction,DeviceId
                    String[] parts = deviceData.split(",");
                    if (parts.length < 8) {
                        failed++;
                        continue;
                    }

                    String biometricUserId = parts[4].trim();  // UserId from biometric device
                    String logDateStr = parts[5].trim();       // LogDate
                    String direction = parts[6].trim();        // Direction (N/A, IN, OUT)
                    String deviceIdStr = parts[7].trim();      // DeviceId

                    // Parse punch time
                    OffsetDateTime punchTime = parsePunchTime(logDateStr);
                    if (punchTime == null) {
                        failed++;
                        continue;
                    }

                    // Filter by date if specified
                    if (fromDate != null && punchTime.toLocalDate().isBefore(fromDate)) {
                        continue;
                    }
                    if (toDate != null && punchTime.toLocalDate().isAfter(toDate)) {
                        continue;
                    }

                    // Find employee by biometric ID
                    Employee employee = employeeCache.computeIfAbsent(biometricUserId,
                        bioId -> findEmployeeByBiometricId(tenantId, bioId));

                    if (employee == null) {
                        skippedNoEmployee++;
                        continue;
                    }

                    // Check for duplicate
                    if (isDuplicatePunch(employee.getId(), punchTime, supabaseId)) {
                        skippedDuplicate++;
                        continue;
                    }

                    // Create PunchLog
                    PunchLog punchLog = new PunchLog();
                    punchLog.setTenantId(tenantId);
                    punchLog.setCompany(company != null ? company : employee.getCompany());
                    punchLog.setEmployee(employee);
                    punchLog.setBiometricId(biometricUserId);
                    punchLog.setDeviceId(deviceIdStr);
                    punchLog.setDeviceName("ESSL Device " + deviceIdStr);
                    punchLog.setLocation(employee.getLocation() != null ? employee.getLocation().getName() : null);
                    punchLog.setPunchTime(punchTime);
                    punchLog.setPunchType(determinePunchType(direction, punchTime, employee.getId()));
                    punchLog.setVerifyMode(VerifyMode.FINGER);
                    punchLog.setPunchSource(PunchSource.BIOMETRIC);
                    punchLog.setStatus(PunchStatus.MATCHED);

                    // Store raw data
                    Map<String, Object> rawData = new HashMap<>();
                    rawData.put("supabaseId", supabaseId);
                    rawData.put("deviceData", deviceData);
                    punchLog.setRawData(rawData);

                    punchLogRepository.save(punchLog);
                    inserted++;

                } catch (Exception e) {
                    log.error("Failed to process record: {}", e.getMessage());
                    failed++;
                }
            }

            String message = String.format("Sync completed: %d fetched, %d inserted, %d duplicates, %d no employee, %d failed",
                totalFetched, inserted, skippedDuplicate, skippedNoEmployee, failed);
            log.info(message);

            return new BiometricSyncResult(totalFetched, inserted, skippedDuplicate, skippedNoEmployee, failed, message);

        } catch (Exception e) {
            log.error("Sync failed: {}", e.getMessage(), e);
            return new BiometricSyncResult(totalFetched, inserted, skippedDuplicate, skippedNoEmployee, failed,
                "Sync failed: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> fetchFromSupabase() {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                supabaseConfig.getBiometricDataEndpoint() + "?select=*&order=created_at.desc",
                HttpMethod.GET,
                entity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                List<Map<String, Object>> records = new ArrayList<>();

                for (JsonNode node : root) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", node.has("id") ? node.get("id").asText() : null);
                    record.put("device_data", node.has("device_data") ? node.get("device_data").asText() : null);
                    record.put("created_at", node.has("created_at") ? node.get("created_at").asText() : null);
                    records.add(record);
                }

                return records;
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch from Supabase: {}", e.getMessage());
            return Collections.emptyList();
        }
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
