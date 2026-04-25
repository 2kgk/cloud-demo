package com.example.testcase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.testcase.entity.ExecutionTask;

import java.util.List;

/**
 * 执行任务服务接口
 */
public interface ExecutionTaskService extends IService<ExecutionTask> {
    
    /**
     * 创建执行任务
     */
    ExecutionTask createTask(String taskName, List<String> caseIds);
    
    /**
     * 根据任务ID查询任务
     */
    ExecutionTask getTaskById(Long taskId);
    
    /**
     * 更新任务状态
     */
    boolean updateTaskStatus(Long taskId, Integer status);
    
    /**
     * 查询所有任务
     */
    List<ExecutionTask> getAllTasks();
}