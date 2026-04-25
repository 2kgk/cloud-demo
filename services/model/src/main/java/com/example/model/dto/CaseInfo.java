package com.example.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用例信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseInfo {
    private String pathId;
    private String caseId;
    private Long projectId;
    private String caseName;
    private String caseDesc;
    private String requestData;
    private String expectedResult;
    private Integer caseType;
    private Integer priority;
}