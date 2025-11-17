package com.hrms.controller;

import com.hrms.entity.Designation;
import com.hrms.repository.DesignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationRepository designationRepository;

    @GetMapping
    public List<Designation> getAllDesignations() {
        return designationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Designation> getDesignationById(@PathVariable Long id) {
        return designationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tenant/{tenantId}")
    public List<Designation> getDesignationsByTenant(@PathVariable String tenantId) {
        return designationRepository.findByTenantId(tenantId);
    }

    @GetMapping("/active")
    public List<Designation> getActiveDesignations() {
        return designationRepository.findByIsActiveTrue();
    }

    @PostMapping
    public ResponseEntity<Designation> createDesignation(@RequestBody Designation designation) {
        Designation saved = designationRepository.save(designation);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Designation> updateDesignation(@PathVariable Long id, @RequestBody Designation designation) {
        return designationRepository.findById(id)
                .map(existing -> {
                    designation.setId(id);
                    return ResponseEntity.ok(designationRepository.save(designation));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDesignation(@PathVariable Long id) {
        if (designationRepository.existsById(id)) {
            designationRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
