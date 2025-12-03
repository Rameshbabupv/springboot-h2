package com.hrms.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateResponse {
    private Long id;
    private String stateName;
    private String stateCode;
}
