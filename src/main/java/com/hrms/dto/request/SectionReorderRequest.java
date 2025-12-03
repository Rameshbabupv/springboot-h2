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
public class SectionReorderRequest {

    @NotNull(message = "Template ID is required")
    private Long templateId;

    @NotEmpty(message = "Section order list is required")
    private List<SectionOrder> sectionOrders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionOrder {
        @NotNull(message = "Section ID is required")
        private Long sectionId;

        @NotNull(message = "Order is required")
        private Integer order;
    }
}
