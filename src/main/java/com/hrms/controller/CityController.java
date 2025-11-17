package com.hrms.controller;

import com.hrms.entity.City;
import com.hrms.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {
    private final CityRepository cityRepository;

    @GetMapping
    public List<City> getAll() {
        return cityRepository.findAll();
    }
}
