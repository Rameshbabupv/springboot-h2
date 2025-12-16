package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictingTemplate {

    /**
     * Conflicting template ID
     */
    private Long id;

    /**
     * Template name
     */
    private String templateName;

    /**
     * Template code
     */
    private String templateCode;

    /**
     * Effective date range
     */
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    /**
     * Conflicting criteria descriptions
     */
    private List<String> conflictingCriteria;

    /**
     * Human-readable conflict description
     */
    private String conflictDescription;
}
