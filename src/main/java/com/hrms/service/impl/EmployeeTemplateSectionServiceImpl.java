package com.hrms.service.impl;

import com.hrms.dto.request.EmployeeTemplateSectionRequest;
import com.hrms.dto.request.SectionReorderRequest;
import com.hrms.dto.response.EmployeeTemplateSectionResponse;
import com.hrms.entity.EmployeeTemplate;
import com.hrms.entity.EmployeeTemplateSection;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.EmployeeTemplateRepository;
import com.hrms.repository.EmployeeTemplateSectionRepository;
import com.hrms.service.EmployeeTemplateSectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeTemplateSectionServiceImpl implements EmployeeTemplateSectionService {

    private final EmployeeTemplateSectionRepository sectionRepository;
    private final EmployeeTemplateRepository templateRepository;

    @Override
    @Transactional
    public EmployeeTemplateSectionResponse createSection(EmployeeTemplateSectionRequest request) {
        log.debug("Creating new template section: {}", request.getSectionName());

        EmployeeTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeTemplate", "id", request.getTemplateId()));

        if (sectionRepository.existsByTemplateIdAndSectionCode(request.getTemplateId(), request.getSectionCode())) {
            throw new DuplicateResourceException("Section with code " + request.getSectionCode() + " already exists for this template");
        }

        EmployeeTemplateSection section = mapToEntity(request, template);
        EmployeeTemplateSection saved = sectionRepository.save(section);

        log.info("Created template section with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeTemplateSectionResponse updateSection(Long id, EmployeeTemplateSectionRequest request) {
        log.debug("Updating template section with id: {}", id);

        EmployeeTemplateSection existing = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeTemplateSection", "id", id));

        // Check for duplicate code if code is being changed
        if (!existing.getSectionCode().equals(request.getSectionCode()) &&
                sectionRepository.existsByTemplateIdAndSectionCode(request.getTemplateId(), request.getSectionCode())) {
            throw new DuplicateResourceException("Section with code " + request.getSectionCode() + " already exists for this template");
        }

        updateEntityFromRequest(existing, request);
        EmployeeTemplateSection updated = sectionRepository.save(existing);

        log.info("Updated template section with id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    public EmployeeTemplateSectionResponse getSectionById(Long id) {
        log.debug("Fetching template section with id: {}", id);
        EmployeeTemplateSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeTemplateSection", "id", id));
        return mapToResponse(section);
    }

    @Override
    public List<EmployeeTemplateSectionResponse> getSectionsByTemplateId(Long templateId) {
        log.debug("Fetching sections for template: {}", templateId);
        return sectionRepository.findByTemplateIdOrderBySectionOrder(templateId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        log.debug("Deleting template section with id: {}", id);

        if (!sectionRepository.existsById(id)) {
            throw new ResourceNotFoundException("EmployeeTemplateSection", "id", id);
        }

        sectionRepository.deleteById(id);
        log.info("Deleted template section with id: {}", id);
    }

    @Override
    @Transactional
    public void reorderSections(SectionReorderRequest request) {
        log.debug("Reordering sections for template: {}", request.getTemplateId());

        for (SectionReorderRequest.SectionOrder sectionOrder : request.getSectionOrders()) {
            EmployeeTemplateSection section = sectionRepository.findById(sectionOrder.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("EmployeeTemplateSection", "id", sectionOrder.getSectionId()));

            section.setSectionOrder(sectionOrder.getOrder());
            sectionRepository.save(section);
        }

        log.info("Reordered sections for template: {}", request.getTemplateId());
    }

    // ==================== Mapping Methods ====================

    private EmployeeTemplateSection mapToEntity(EmployeeTemplateSectionRequest request, EmployeeTemplate template) {
        EmployeeTemplateSection section = new EmployeeTemplateSection();
        section.setTemplate(template);
        section.setSectionName(request.getSectionName());
        section.setSectionCode(request.getSectionCode());
        section.setSectionDescription(request.getSectionDescription());
        section.setSectionOrder(request.getSectionOrder());
        section.setSectionIcon(request.getSectionIcon());
        section.setIsCollapsible(request.getIsCollapsible() != null ? request.getIsCollapsible() : true);
        section.setIsExpandedByDefault(request.getIsExpandedByDefault() != null ? request.getIsExpandedByDefault() : true);
        section.setConditionalLogic(request.getConditionalLogic());
        return section;
    }

    private void updateEntityFromRequest(EmployeeTemplateSection section, EmployeeTemplateSectionRequest request) {
        section.setSectionName(request.getSectionName());
        section.setSectionCode(request.getSectionCode());
        section.setSectionDescription(request.getSectionDescription());
        section.setSectionOrder(request.getSectionOrder());
        section.setSectionIcon(request.getSectionIcon());
        if (request.getIsCollapsible() != null) section.setIsCollapsible(request.getIsCollapsible());
        if (request.getIsExpandedByDefault() != null) section.setIsExpandedByDefault(request.getIsExpandedByDefault());
        section.setConditionalLogic(request.getConditionalLogic());
    }

    private EmployeeTemplateSectionResponse mapToResponse(EmployeeTemplateSection section) {
        return EmployeeTemplateSectionResponse.builder()
                .id(section.getId())
                .templateId(section.getTemplate().getId())
                .sectionName(section.getSectionName())
                .sectionCode(section.getSectionCode())
                .sectionDescription(section.getSectionDescription())
                .sectionOrder(section.getSectionOrder())
                .sectionIcon(section.getSectionIcon())
                .isCollapsible(section.getIsCollapsible())
                .isExpandedByDefault(section.getIsExpandedByDefault())
                .conditionalLogic(section.getConditionalLogic())
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .fields(new ArrayList<>())
                .build();
    }
}
