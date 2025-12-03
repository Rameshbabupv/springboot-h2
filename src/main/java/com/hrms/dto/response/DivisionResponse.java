package com.hrms.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DivisionResponse {
    private Long id;
    private String divisionName;
    private String divisionCode;
}
