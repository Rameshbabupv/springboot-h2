package com.hrms.controller;

import com.hrms.entity.State;
import com.hrms.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/states")
@RequiredArgsConstructor
public class StateController {
    private final StateRepository stateRepository;

    @GetMapping
    public List<State> getAll() {
        return stateRepository.findAll();
    }
}
