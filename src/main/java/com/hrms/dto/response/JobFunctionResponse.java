package com.hrms.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobFunctionResponse {
    private Long id;
    private String jobFunctionName;
    private String jobFunctionCode;
}
