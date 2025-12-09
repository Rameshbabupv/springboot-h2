package com.hrms.graphql.input;

import lombok.Data;

/**
 * GraphQL input for Excel import row data.
 * Frontend parses Excel and sends JSON array.
 */
@Data
public class ExcelRowInput {
    private Integer rowNumber;
    private String employeeId;      // Employee code (e.g., "EMP001")
    private String date;            // DD/MM/YYYY format
    private String inTime;          // HH:MM (24h)
    private String outTime;         // HH:MM (24h)
    private String status;          // P, A, HD, WO, H, L, OD
    private String remarks;
}
