package com.hrms.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentTypeResponse {
    private Long id;
    private String employmentTypeName;
    private String employmentTypeCode;
}
