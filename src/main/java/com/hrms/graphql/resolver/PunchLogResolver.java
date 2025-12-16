package com.hrms.graphql.resolver;

import com.hrms.entity.Company;
import com.hrms.entity.Employee;
import com.hrms.entity.PunchLog;
import com.hrms.enums.PunchStatus;
import com.hrms.graphql.input.PunchLogInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.PunchLogService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * GraphQL resolver for PunchLog operations.
 */
@Controller
public class PunchLogResolver {

    private final PunchLogService punchLogService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    public PunchLogResolver(PunchLogService punchLogService, JwtClaimsExtractor jwtClaimsExtractor) {
        this.punchLogService = punchLogService;
        this.jwtClaimsExtractor = jwtClaimsExtractor;
    }

    // Schema Mappings for nested objects

    @SchemaMapping(typeName = "PunchLog", field = "employee")
    public Employee employee(PunchLog punchLog) {
        return punchLog.getEmployee();
    }

    @SchemaMapping(typeName = "PunchLog", field = "company")
    public Company company(PunchLog punchLog) {
        return punchLog.getCompany();
    }

    // Queries

    @QueryMapping
    public List<PunchLog> punchLogs(@Argument(name = "tenantId") String tenantIdArg,
                                     @Argument Long companyId,
                                     @Argument String dateFrom,
                                     @Argument String dateTo,
                                     @Argument Long employeeId,
                                     @Argument PunchStatus status) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        OffsetDateTime from = parseDateTime(dateFrom);
        OffsetDateTime to = parseDateTime(dateTo);
        return punchLogService.getPunchLogs(tenantId, companyId, from, to, employeeId, status);
    }

    // Mutations

    @MutationMapping
    public PunchLog recordPunch(@Argument(name = "tenantId") String tenantIdArg,
                                @Argument Long companyId,
                                @Argument PunchLogInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return punchLogService.recordPunch(tenantId, companyId, input);
    }

    @MutationMapping
    public Integer bulkImportPunches(@Argument(name = "tenantId") String tenantIdArg,
                                     @Argument Long companyId,
                                     @Argument List<PunchLogInput> inputs) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return punchLogService.bulkImportPunches(tenantId, companyId, inputs);
    }

    @MutationMapping
    public PunchLog mapUnmatchedPunch(@Argument(name = "tenantId") String tenantIdArg,
                                      @Argument Long punchLogId,
                                      @Argument Long employeeId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return punchLogService.mapUnmatchedPunch(tenantId, punchLogId, employeeId);
    }

    private OffsetDateTime parseDateTime(String dateTime) {
        if (dateTime == null) return null;
        // Handle date-only format (e.g., "2025-12-05")
        if (dateTime.length() == 10 && !dateTime.contains("T")) {
            return LocalDate.parse(dateTime)
                    .atStartOfDay()
                    .atZone(ZoneId.of("Asia/Kolkata"))
                    .toOffsetDateTime();
        }
        return OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
    }
}
