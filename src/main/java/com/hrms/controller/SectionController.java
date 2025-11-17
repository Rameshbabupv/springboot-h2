package com.hrms.controller;

import com.hrms.entity.Section;
import com.hrms.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {
    private final SectionRepository sectionRepository;

    @GetMapping
    public List<Section> getAll() {
        return sectionRepository.findAll();
    }
}
