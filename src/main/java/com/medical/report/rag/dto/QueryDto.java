package com.medical.report.rag.dto;

import lombok.Data;

@Data
public class QueryDto {
    String query;
    String patientId;
}
