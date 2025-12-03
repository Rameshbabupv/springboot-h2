package com.hrms.service;

import com.hrms.dto.request.FieldDefinitionMasterRequest;
import com.hrms.dto.response.FieldDefinitionMasterResponse;

import java.util.List;
import java.util.UUID;

public interface FieldDefinitionMasterService {

    FieldDefinitionMasterResponse createField(FieldDefinitionMasterRequest request);

    FieldDefinitionMasterResponse updateField(Long id, FieldDefinitionMasterRequest request);

    FieldDefinitionMasterResponse getFieldById(Long id);

    List<FieldDefinitionMasterResponse> getAllFields(String tenantId);

    List<FieldDefinitionMasterResponse> getFieldsByCategory(String tenantId, String category);

    List<FieldDefinitionMasterResponse> getSystemFields(String tenantId);

    List<FieldDefinitionMasterResponse> getCustomFields(String tenantId);

    void deleteField(Long id);
}
