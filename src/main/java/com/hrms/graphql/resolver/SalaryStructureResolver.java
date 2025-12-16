package com.hrms.graphql.resolver;

import com.hrms.dto.response.SalarySummary;
import com.hrms.dto.response.SalarySummaryCalculated;
import com.hrms.entity.EmployeeSalaryStructure;
import com.hrms.graphql.input.SalaryComponentInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.SalaryStructureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;

/**
 * GraphQL Resolver for Employee Salary Structure operations
 *
 * JWT Integration:
 * - tenantId is extracted from JWT token (preferred)
 * - @Argument tenantId kept for backward compatibility during migration
 * - JWT takes precedence when available
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class SalaryStructureResolver {

    private final SalaryStructureService salaryStructureService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    // ==================== Queries ====================

    @QueryMapping
    public List<EmployeeSalaryStructure> employeeSalaryStructure(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long companyId,
            @Argument Long employeeId,
            @Argument String effectiveDate) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.debug("GraphQL Query: employeeSalaryStructure - employee: {}, date: {}",
                  employeeId, effectiveDate);

        LocalDate date = effectiveDate != null ? LocalDate.parse(effectiveDate) : null;

        return salaryStructureService.getEmployeeSalaryStructure(tenantId, companyId, employeeId, date);
    }

    @QueryMapping
    public SalarySummary employeeSalarySummary(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long companyId,
            @Argument Long employeeId,
            @Argument String effectiveDate) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.debug("GraphQL Query: employeeSalarySummary - employee: {}, date: {}",
                  employeeId, effectiveDate);

        LocalDate date = effectiveDate != null ? LocalDate.parse(effectiveDate) : LocalDate.now();

        return salaryStructureService.calculateSalarySummary(tenantId, companyId, employeeId, date);
    }

    @QueryMapping
    public SalarySummaryCalculated calculateSalarySummary(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long companyId,
            @Argument List<SalaryComponentInput> salaryComponents,
            @Argument String effectiveDate) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.debug("GraphQL Query: calculateSalarySummary - tenant: {}, company: {}, components: {}",
                  tenantId, companyId, salaryComponents != null ? salaryComponents.size() : 0);

        LocalDate date = effectiveDate != null ? LocalDate.parse(effectiveDate) : LocalDate.now();

        return salaryStructureService.batchCalculateSalary(tenantId, companyId, salaryComponents, date);
    }

    // ==================== Mutations ====================

    @MutationMapping
    public List<EmployeeSalaryStructure> saveEmployeeSalaryStructure(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long companyId,
            @Argument Long employeeId,
            @Argument List<SalaryComponentInput> salaryComponents,
            @Argument String effectiveFrom) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.info("GraphQL Mutation: saveEmployeeSalaryStructure - employee: {}, components: {}, date: {}",
                 employeeId, salaryComponents.size(), effectiveFrom);

        LocalDate date = LocalDate.parse(effectiveFrom);

        return salaryStructureService.saveEmployeeSalaryStructure(
            tenantId, companyId, employeeId, salaryComponents, date);
    }

    @MutationMapping
    public List<EmployeeSalaryStructure> reviseEmployeeSalary(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument Long companyId,
            @Argument Long employeeId,
            @Argument List<SalaryComponentInput> salaryComponents,
            @Argument String effectiveFrom,
            @Argument String remarks) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.info("GraphQL Mutation: reviseEmployeeSalary - employee: {}, date: {}, remarks: {}",
                 employeeId, effectiveFrom, remarks);

        LocalDate date = LocalDate.parse(effectiveFrom);

        return salaryStructureService.reviseEmployeeSalary(
            tenantId, companyId, employeeId, salaryComponents, date, remarks);
    }
}
