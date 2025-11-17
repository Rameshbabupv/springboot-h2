package com.hrms.controller;

import com.hrms.entity.Division;
import com.hrms.repository.DivisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/divisions")
@RequiredArgsConstructor
public class DivisionController {
    private final DivisionRepository divisionRepository;

    @GetMapping
    public List<Division> getAll() {
        return divisionRepository.findAll();
    }
}
