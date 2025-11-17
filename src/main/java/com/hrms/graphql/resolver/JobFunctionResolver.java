package com.hrms.graphql.resolver;

import com.hrms.entity.JobFunction;
import com.hrms.graphql.input.JobFunctionInput;
import com.hrms.repository.JobFunctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class JobFunctionResolver {

    private final JobFunctionRepository jobFunctionRepository;

    @QueryMapping
    public List<JobFunction> jobFunctions() {
        return jobFunctionRepository.findAll();
    }

    @QueryMapping
    public JobFunction jobFunction(@Argument Long id) {
        return jobFunctionRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<JobFunction> jobFunctionsByTenant(@Argument String tenantId) {
        return jobFunctionRepository.findByTenantId(tenantId);
    }

    @QueryMapping
    public List<JobFunction> activeJobFunctions() {
        return jobFunctionRepository.findByIsActiveTrue();
    }

    @MutationMapping
    public JobFunction createJobFunction(@Argument JobFunctionInput input) {
        JobFunction jobFunction = mapToEntity(input);
        return jobFunctionRepository.save(jobFunction);
    }

    @MutationMapping
    public JobFunction updateJobFunction(@Argument Long id, @Argument JobFunctionInput input) {
        JobFunction jobFunction = jobFunctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobFunction not found"));

        updateEntityFromInput(jobFunction, input);
        return jobFunctionRepository.save(jobFunction);
    }

    @MutationMapping
    public Boolean deleteJobFunction(@Argument Long id) {
        if (jobFunctionRepository.existsById(id)) {
            jobFunctionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private JobFunction mapToEntity(JobFunctionInput input) {
        JobFunction jobFunction = new JobFunction();
        updateEntityFromInput(jobFunction, input);
        return jobFunction;
    }

    private void updateEntityFromInput(JobFunction jobFunction, JobFunctionInput input) {
        jobFunction.setTenantId(input.getTenantId());
        jobFunction.setName(input.getName());
        jobFunction.setCode(input.getCode());
        jobFunction.setDescription(input.getDescription());
        jobFunction.setIsActive(input.getIsActive());
    }
}
