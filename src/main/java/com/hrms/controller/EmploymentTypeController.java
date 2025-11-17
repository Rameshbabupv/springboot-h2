package com.hrms.controller;

import com.hrms.entity.EmploymentType;
import com.hrms.repository.EmploymentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employment-types")
@RequiredArgsConstructor
public class EmploymentTypeController {
    private final EmploymentTypeRepository employmentTypeRepository;

    @GetMapping
    public List<EmploymentType> getAll() {
        return employmentTypeRepository.findAll();
    }
}
