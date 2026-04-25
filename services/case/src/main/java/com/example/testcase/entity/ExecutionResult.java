package com.example.testcase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行结果实体类
 */
@Data
@TableName("execution_result")
public class ExecutionResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String caseId;
    private String caseTableSuffix;
    private Integer executionResult;
    private Integer executionTime;
    private String errorMessage;
    private String actualResult;
    private Integer assertResult;
    private Integer deleted;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}