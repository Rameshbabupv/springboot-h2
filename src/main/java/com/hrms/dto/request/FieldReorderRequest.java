package com.hrms.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldReorderRequest {

    @NotNull(message = "Section ID is required")
    private Long sectionId;

    @NotEmpty(message = "Field order list is required")
    private List<FieldOrder> fieldOrders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldOrder {
        @NotNull(message = "Field ID is required")
        private Long fieldId;

        @NotNull(message = "Order is required")
        private Integer order;
    }
}
