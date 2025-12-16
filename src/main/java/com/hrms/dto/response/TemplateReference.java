package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateReference {

    /**
     * Template ID
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
     * Effective from date
     */
    private LocalDate effectiveFrom;

    /**
     * Effective to date
     */
    private LocalDate effectiveTo;
}
