package com.example.testcase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型批次进度实体类
 */
@Data
@TableName("model_batch_progress")
public class ModelBatchProgress {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskId;
    private Long modelId;
    private Long suiteId;
    private Integer totalBatchCount;
    private Integer currentBatchIndex;
    private Integer processedBatchCount;
    private String processedBatchIndexes; // 已处理的批次索引列表，逗号分隔，如"0,1,2,3"
    private Integer status; // 1-进行中, 2-已完成
    private String message;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer deleted;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}