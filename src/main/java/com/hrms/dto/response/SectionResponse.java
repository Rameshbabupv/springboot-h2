package com.hrms.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectionResponse {
    private Long id;
    private String sectionName;
    private String sectionCode;
}
