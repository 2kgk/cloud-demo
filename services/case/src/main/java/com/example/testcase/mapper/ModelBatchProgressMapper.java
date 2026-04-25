package com.example.testcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.testcase.entity.ModelBatchProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 模型批次进度Mapper接口
 */
@Mapper
public interface ModelBatchProgressMapper extends BaseMapper<ModelBatchProgress> {
    
    /**
     * 根据任务ID查询批次进度
     */
    ModelBatchProgress selectByTaskId(@Param("taskId") String taskId);
    
    /**
     * 根据任务ID和批次索引查询批次进度
     */
    ModelBatchProgress selectByTaskIdAndBatchIndex(@Param("taskId") String taskId, @Param("batchIndex") Integer batchIndex);
}