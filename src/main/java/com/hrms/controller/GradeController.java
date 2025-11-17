package com.hrms.controller;

import com.hrms.entity.Grade;
import com.hrms.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {
    private final GradeRepository gradeRepository;

    @GetMapping
    public List<Grade> getAll() {
        return gradeRepository.findAll();
    }
}
