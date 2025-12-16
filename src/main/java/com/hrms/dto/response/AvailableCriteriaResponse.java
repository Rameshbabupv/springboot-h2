package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableCriteriaResponse {

    /**
     * Available (unassigned) criteria values
     */
    private List<CriteriaValue> available;

    /**
     * Already assigned criteria values with template references
     */
    private List<AssignedCriteriaValue> assigned;

    /**
     * Total count of available values
     */
    private Integer totalAvailable;

    /**
     * Total count of assigned values
     */
    private Integer totalAssigned;
}
