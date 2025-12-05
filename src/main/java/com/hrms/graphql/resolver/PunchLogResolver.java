package com.hrms.graphql.resolver;

import com.hrms.entity.PunchLog;
import com.hrms.enums.PunchStatus;
import com.hrms.graphql.input.PunchLogInput;
import com.hrms.service.PunchLogService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * GraphQL resolver for PunchLog operations.
 */
@Controller
public class PunchLogResolver {

    private final PunchLogService punchLogService;

    public PunchLogResolver(PunchLogService punchLogService) {
        this.punchLogService = punchLogService;
    }

    // Queries

    @QueryMapping
    public List<PunchLog> punchLogs(@Argument String tenantId,
                                     @Argument Long companyId,
                                     @Argument String dateFrom,
                                     @Argument String dateTo,
                                     @Argument Long employeeId,
                                     @Argument PunchStatus status) {
        OffsetDateTime from = parseDateTime(dateFrom);
        OffsetDateTime to = parseDateTime(dateTo);
        return punchLogService.getPunchLogs(tenantId, companyId, from, to, employeeId, status);
    }

    // Mutations

    @MutationMapping
    public PunchLog recordPunch(@Argument String tenantId,
                                @Argument Long companyId,
                                @Argument PunchLogInput input) {
        return punchLogService.recordPunch(tenantId, companyId, input);
    }

    @MutationMapping
    public Integer bulkImportPunches(@Argument String tenantId,
                                     @Argument Long companyId,
                                     @Argument List<PunchLogInput> inputs) {
        return punchLogService.bulkImportPunches(tenantId, companyId, inputs);
    }

    @MutationMapping
    public PunchLog mapUnmatchedPunch(@Argument String tenantId,
                                      @Argument Long punchLogId,
                                      @Argument Long employeeId) {
        return punchLogService.mapUnmatchedPunch(tenantId, punchLogId, employeeId);
    }

    private OffsetDateTime parseDateTime(String dateTime) {
        if (dateTime == null) return null;
        return OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
    }
}
