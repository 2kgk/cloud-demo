package com.example.testcase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.testcase.entity.ModelBatchProgress;
import com.example.testcase.mapper.ModelBatchProgressMapper;
import com.example.testcase.service.ModelBatchProgressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 模型批次进度服务实现类
 */
@Service
public class ModelBatchProgressServiceImpl extends ServiceImpl<ModelBatchProgressMapper, ModelBatchProgress> implements ModelBatchProgressService {
    
    @Override
    public ModelBatchProgress getByTaskId(String taskId) {
        LambdaQueryWrapper<ModelBatchProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModelBatchProgress::getTaskId, taskId)
                   .eq(ModelBatchProgress::getDeleted, 0);
        return getOne(queryWrapper);
    }
    
    @Override
    public ModelBatchProgress getByTaskIdAndBatchIndex(String taskId, Integer batchIndex) {
        LambdaQueryWrapper<ModelBatchProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModelBatchProgress::getTaskId, taskId)
                   .eq(ModelBatchProgress::getCurrentBatchIndex, batchIndex)
                   .eq(ModelBatchProgress::getDeleted, 0);
        return getOne(queryWrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelBatchProgress initBatchProgress(String taskId, Long modelId, Long suiteId, Integer totalBatchCount) {
        ModelBatchProgress progress = new ModelBatchProgress();
        progress.setTaskId(taskId);
        progress.setModelId(modelId);
        progress.setSuiteId(suiteId);
        progress.setTotalBatchCount(totalBatchCount);
        progress.setCurrentBatchIndex(0);
        progress.setProcessedBatchCount(0);
        progress.setProcessedBatchIndexes(""); // 初始化为空字符串
        progress.setStatus(1); // 1-进行中
        progress.setMessage("任务开始处理");
        progress.setStartTime(LocalDateTime.now());
        progress.setDeleted(0);
        progress.setCreatedTime(LocalDateTime.now());
        progress.setUpdatedTime(LocalDateTime.now());
        
        save(progress);
        return progress;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProcessedBatch(String taskId, Integer batchIndex) {
        ModelBatchProgress progress = getByTaskId(taskId);
        if (progress == null) {
            return false;
        }
        
        // 检查该批次是否已经处理过（幂等性检查）
        if (isBatchIndexProcessed(progress.getProcessedBatchIndexes(), batchIndex)) {
            return true; // 已处理，直接返回成功
        }
        
        // 更新当前批次索引为最大值（支持乱序消费）
        if (progress.getCurrentBatchIndex() == null || batchIndex > progress.getCurrentBatchIndex()) {
            progress.setCurrentBatchIndex(batchIndex);
        }
        
        // 将当前批次索引添加到已处理列表
        String updatedIndexes = appendBatchIndex(progress.getProcessedBatchIndexes(), batchIndex);
        progress.setProcessedBatchIndexes(updatedIndexes);
        
        // 增加已处理批次数
        progress.setProcessedBatchCount(progress.getProcessedBatchCount() + 1);
        progress.setUpdatedTime(LocalDateTime.now());
        
        return updateById(progress);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsCompleted(String taskId) {
        ModelBatchProgress progress = getByTaskId(taskId);
        if (progress == null) {
            return false;
        }
        
        progress.setStatus(2); // 2-已完成
        progress.setMessage("任务处理完成");
        progress.setEndTime(LocalDateTime.now());
        progress.setUpdatedTime(LocalDateTime.now());
        
        return updateById(progress);
    }
    
    @Override
    public boolean isBatchProcessed(String taskId, Integer batchIndex) {
        ModelBatchProgress progress = getByTaskId(taskId);
        if (progress == null) {
            return false;
        }
        
        // 检查该批次索引是否已在已处理列表中
        return isBatchIndexProcessed(progress.getProcessedBatchIndexes(), batchIndex);
    }
    
    /**
     * 检查批次索引是否已处理
     */
    private boolean isBatchIndexProcessed(String processedBatchIndexes, Integer batchIndex) {
        if (processedBatchIndexes == null || processedBatchIndexes.isEmpty()) {
            return false;
        }
        
        // 将逗号分隔的字符串转换为数组进行检查
        String[] indexes = processedBatchIndexes.split(",");
        for (String index : indexes) {
            if (index.trim().equals(String.valueOf(batchIndex))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 将批次索引添加到已处理列表
     */
    private String appendBatchIndex(String processedBatchIndexes, Integer batchIndex) {
        if (processedBatchIndexes == null || processedBatchIndexes.isEmpty()) {
            return String.valueOf(batchIndex);
        }
        
        // 避免重复添加
        if (isBatchIndexProcessed(processedBatchIndexes, batchIndex)) {
            return processedBatchIndexes;
        }
        
        return processedBatchIndexes + "," + batchIndex;
    }
}