package com.example.testcase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.testcase.entity.ModelBatchProgress;

/**
 * 模型批次进度服务接口
 */
public interface ModelBatchProgressService extends IService<ModelBatchProgress> {
    
    /**
     * 根据任务ID查询批次进度
     */
    ModelBatchProgress getByTaskId(String taskId);
    
    /**
     * 根据任务ID和批次索引查询批次进度
     */
    ModelBatchProgress getByTaskIdAndBatchIndex(String taskId, Integer batchIndex);
    
    /**
     * 初始化批次进度记录
     */
    ModelBatchProgress initBatchProgress(String taskId, Long modelId, Long suiteId, Integer totalBatchCount);
    
    /**
     * 更新批次进度（增加已处理批次数）
     */
    boolean updateProcessedBatch(String taskId, Integer batchIndex);
    
    /**
     * 标记批次任务为完成
     */
    boolean markAsCompleted(String taskId);
    
    /**
     * 检查批次是否已处理（幂等性检查）
     */
    boolean isBatchProcessed(String taskId, Integer batchIndex);
}