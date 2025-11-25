package com.hrms.service;

import com.hrms.dto.request.DivisionRequest;
import com.hrms.entity.Division;

import java.util.List;

/**
 * Service interface for Division operations.
 */
public interface DivisionService {

    List<Division> getAllDivisions();

    Division getDivisionById(Long id);

    List<Division> getDivisionsByTenant(String tenantId);

    List<Division> getActiveDivisions();

    Division createDivision(DivisionRequest request);

    Division updateDivision(Long id, DivisionRequest request);

    void deleteDivision(Long id);

    boolean existsById(Long id);
}
