package com.hrms.graphql.resolver;

import com.hrms.entity.AttendanceImportError;
import com.hrms.entity.AttendanceImportLog;
import com.hrms.entity.Company;
import com.hrms.entity.UserAccount;
import com.hrms.enums.ImportStatus;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.AttendanceImportLogService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * GraphQL resolver for AttendanceImportLog operations.
 */
@Controller
public class AttendanceImportLogResolver {

    private final AttendanceImportLogService importLogService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    public AttendanceImportLogResolver(AttendanceImportLogService importLogService,
                                      JwtClaimsExtractor jwtClaimsExtractor) {
        this.importLogService = importLogService;
        this.jwtClaimsExtractor = jwtClaimsExtractor;
    }

    // Schema Mappings for nested objects

    @SchemaMapping(typeName = "AttendanceImportLog", field = "company")
    public Company company(AttendanceImportLog importLog) {
        return importLog.getCompany();
    }

    @SchemaMapping(typeName = "AttendanceImportLog", field = "importedBy")
    public UserAccount importedBy(AttendanceImportLog importLog) {
        return importLog.getImportedBy();
    }

    // Queries

    @QueryMapping
    public AttendanceImportLogService.ImportLogPage getAttendanceImportHistory(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long companyId,
            @Argument Integer limit,
            @Argument Integer offset,
            @Argument ImportStatus status,
            @Argument String fromDate,
            @Argument String toDate) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        int actualLimit = limit != null ? limit : 10;
        int actualOffset = offset != null ? offset : 0;
        OffsetDateTime from = fromDate != null ? parseDateTime(fromDate) : null;
        OffsetDateTime to = toDate != null ? parseDateTime(toDate) : null;

        return importLogService.getImportHistory(tenantId, companyId, actualLimit, actualOffset, status, from, to);
    }

    @QueryMapping
    public AttendanceImportLog getAttendanceImportDetails(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long importId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return importLogService.getImportDetails(tenantId, importId).orElse(null);
    }

    @QueryMapping
    public List<AttendanceImportError> getImportErrors(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long importId,
            @Argument Integer limit,
            @Argument Integer offset) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        int actualLimit = limit != null ? limit : 50;
        int actualOffset = offset != null ? offset : 0;

        return importLogService.getImportErrors(tenantId, importId, actualLimit, actualOffset);
    }

    // Helper methods

    private OffsetDateTime parseDateTime(String dateTime) {
        if (dateTime == null) return null;
        // Try parsing as ISO datetime
        try {
            return OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            // Try as date only and add time
            try {
                return java.time.LocalDate.parse(dateTime, DateTimeFormatter.ISO_DATE)
                        .atStartOfDay()
                        .atOffset(java.time.ZoneOffset.UTC);
            } catch (Exception e2) {
                throw new IllegalArgumentException("Invalid date format: " + dateTime);
            }
        }
    }
}
