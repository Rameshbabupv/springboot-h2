package com.hrms.service;

import com.hrms.dto.request.GradeRequest;
import com.hrms.entity.Grade;

import java.util.List;

/**
 * Service interface for Grade operations.
 */
public interface GradeService {

    List<Grade> getAllGrades();

    Grade getGradeById(Long id);

    List<Grade> getGradesByTenant(String tenantId);

    List<Grade> getActiveGradesByTenant(String tenantId);

    List<Grade> getActiveGrades();

    List<Grade> searchGrades(String tenantId, String searchTerm);

    Grade createGrade(GradeRequest request);

    Grade updateGrade(Long id, GradeRequest request);

    void deleteGrade(Long id);

    boolean existsById(Long id);

    /**
     * Get grades for selection with organizational scope filtering.
     */
    List<Grade> getGradesForSelection(String tenantId, Long userId, boolean isEditMode, Long currentGradeId);
}
