package com.hrms.controller;

import com.hrms.entity.JobFunction;
import com.hrms.repository.JobFunctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-functions")
@RequiredArgsConstructor
public class JobFunctionController {
    private final JobFunctionRepository jobFunctionRepository;

    @GetMapping
    public List<JobFunction> getAll() {
        return jobFunctionRepository.findAll();
    }
}
