package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriteriaValue {

    /**
     * Entity ID
     */
    private Long id;

    /**
     * Display name
     */
    private String name;

    /**
     * Entity code (if applicable)
     */
    private String code;

    /**
     * Active status
     */
    private Boolean isActive;
}
