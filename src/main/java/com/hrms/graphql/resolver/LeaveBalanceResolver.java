package com.hrms.graphql.resolver;

import com.hrms.entity.LeaveBalance;
import com.hrms.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * GraphQL Resolver for LeaveBalance operations.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class LeaveBalanceResolver {

    private final LeaveBalanceService leaveBalanceService;

    @QueryMapping
    public List<LeaveBalance> employeeLeaveBalances(@Argument String tenantId,
                                                     @Argument Long companyId,
                                                     @Argument Long employeeId,
                                                     @Argument String leaveYear) {
        log.debug("GraphQL Query: employeeLeaveBalances - tenantId: {}, companyId: {}, employeeId: {}, leaveYear: {}",
                  tenantId, companyId, employeeId, leaveYear);
        return leaveBalanceService.getEmployeeBalances(tenantId, companyId, employeeId, leaveYear);
    }

    @QueryMapping
    public List<LeaveBalance> leaveBalancesByCompany(@Argument String tenantId,
                                                      @Argument Long companyId,
                                                      @Argument String leaveYear) {
        log.debug("GraphQL Query: leaveBalancesByCompany - tenantId: {}, companyId: {}, leaveYear: {}",
                  tenantId, companyId, leaveYear);
        return leaveBalanceService.getBalancesByCompany(tenantId, companyId, leaveYear);
    }

    @MutationMapping
    public LeaveBalance adjustLeaveBalance(@Argument Long id,
                                            @Argument Map<String, Object> input) {
        log.debug("GraphQL Mutation: adjustLeaveBalance - id: {}", id);
        BigDecimal adjustment = new BigDecimal(input.get("adjustment").toString());
        String reason = (String) input.get("reason");
        return leaveBalanceService.adjustBalance(id, adjustment, reason);
    }

    @MutationMapping
    public LeaveBalance creditLeaveBalance(@Argument String tenantId,
                                            @Argument Long employeeId,
                                            @Argument Long leaveTypeId,
                                            @Argument String leaveYear,
                                            @Argument Double amount) {
        log.debug("GraphQL Mutation: creditLeaveBalance - employeeId: {}, leaveTypeId: {}, amount: {}",
                  employeeId, leaveTypeId, amount);
        return leaveBalanceService.creditBalance(tenantId, employeeId, leaveTypeId, leaveYear,
                                                  BigDecimal.valueOf(amount));
    }

    @MutationMapping
    public List<LeaveBalance> initializeEmployeeBalances(@Argument String tenantId,
                                                          @Argument Long companyId,
                                                          @Argument Long employeeId,
                                                          @Argument String leaveYear) {
        log.debug("GraphQL Mutation: initializeEmployeeBalances - employeeId: {}, leaveYear: {}",
                  employeeId, leaveYear);
        return leaveBalanceService.initializeEmployeeBalances(tenantId, companyId, employeeId, leaveYear);
    }
}
