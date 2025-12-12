package com.hrms.dto.response;

import com.hrms.entity.EmployeeHolidaySelection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for submitting employee holiday selections mutation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitEmployeeHolidaySelectionsResponse {
    private Boolean success;
    private String message;
    private List<EmployeeHolidaySelection> selections;
    private Integer selectedCount;
}
