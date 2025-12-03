package com.hrms.graphql.resolver;

import com.hrms.dto.request.JobFunctionRequest;
import com.hrms.entity.JobFunction;
import com.hrms.graphql.input.JobFunctionInput;
import com.hrms.service.JobFunctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class JobFunctionResolver {

    private final JobFunctionService jobFunctionService;

    @QueryMapping
    public List<JobFunction> jobFunctions() {
        return jobFunctionService.getAllJobFunctions();
    }

    @QueryMapping
    public JobFunction jobFunction(@Argument Long id) {
        return jobFunctionService.getJobFunctionById(id);
    }

    @QueryMapping
    public List<JobFunction> jobFunctionsByTenant(@Argument String tenantId) {
        return jobFunctionService.getJobFunctionsByTenant(tenantId);
    }

    @QueryMapping
    public List<JobFunction> activeJobFunctionsByTenant(@Argument String tenantId) {
        return jobFunctionService.getActiveJobFunctionsByTenant(tenantId);
    }

    @QueryMapping
    public List<JobFunction> activeJobFunctions() {
        return jobFunctionService.getActiveJobFunctions();
    }

    @QueryMapping
    public List<JobFunction> jobFunctionsByGroup(@Argument String tenantId, @Argument String functionGroup) {
        return jobFunctionService.getJobFunctionsByGroup(tenantId, functionGroup);
    }

    @QueryMapping
    public List<JobFunction> searchJobFunctions(@Argument String tenantId, @Argument String searchTerm) {
        return jobFunctionService.searchJobFunctions(tenantId, searchTerm);
    }

    @MutationMapping
    public JobFunction createJobFunction(@Argument JobFunctionInput input) {
        JobFunctionRequest request = mapToRequest(input);
        return jobFunctionService.createJobFunction(request);
    }

    @MutationMapping
    public JobFunction updateJobFunction(@Argument Long id, @Argument JobFunctionInput input) {
        JobFunctionRequest request = mapToRequest(input);
        return jobFunctionService.updateJobFunction(id, request);
    }

    @MutationMapping
    public Boolean deleteJobFunction(@Argument Long id) {
        jobFunctionService.deleteJobFunction(id);
        return true;
    }

    private JobFunctionRequest mapToRequest(JobFunctionInput input) {
        return JobFunctionRequest.builder()
                .tenantId(input.getTenantId())
                .name(input.getName())
                .code(input.getCode())
                .description(input.getDescription())
                .functionGroup(input.getFunctionGroup())
                .isActive(input.getIsActive())
                .build();
    }
}
