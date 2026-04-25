package com.example.testcase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 执行任务实体类
 */
@Data
@TableName("execution_task")
public class ExecutionTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskName;
    private String caseIds;
    private Integer taskStatus;
    private Integer totalCases;
    private Integer successCases;
    private Integer failedCases;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer deleted;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}