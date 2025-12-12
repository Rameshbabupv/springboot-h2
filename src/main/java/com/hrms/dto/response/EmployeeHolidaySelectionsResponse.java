package com.hrms.dto.response;

import com.hrms.entity.EmployeeHolidaySelection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for employee holiday selections query.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHolidaySelectionsResponse {
    private List<EmployeeHolidaySelection> selections;
    private Boolean isSubmitted;
    private LocalDateTime submittedAt;
    private Integer allowedCount;
    private Integer selectedCount;
}
