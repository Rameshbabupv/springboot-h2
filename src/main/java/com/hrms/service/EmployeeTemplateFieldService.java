package com.hrms.service;

import com.hrms.dto.request.EmployeeTemplateFieldRequest;
import com.hrms.dto.request.FieldReorderRequest;
import com.hrms.dto.response.EmployeeTemplateFieldResponse;

import java.util.List;

public interface EmployeeTemplateFieldService {

    EmployeeTemplateFieldResponse createField(EmployeeTemplateFieldRequest request);

    EmployeeTemplateFieldResponse updateField(Long id, EmployeeTemplateFieldRequest request);

    EmployeeTemplateFieldResponse getFieldById(Long id);

    List<EmployeeTemplateFieldResponse> getFieldsBySectionId(Long sectionId);

    List<EmployeeTemplateFieldResponse> getFieldsByTemplateId(Long templateId);

    void deleteField(Long id);

    void reorderFields(FieldReorderRequest request);
}
