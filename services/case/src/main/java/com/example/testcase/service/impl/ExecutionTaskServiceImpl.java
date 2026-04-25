package com.example.testcase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.testcase.entity.ExecutionTask;
import com.example.testcase.mapper.ExecutionTaskMapper;
import com.example.testcase.service.ExecutionTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 执行任务服务实现类
 */
@Service
public class ExecutionTaskServiceImpl extends ServiceImpl<ExecutionTaskMapper, ExecutionTask> implements ExecutionTaskService {
    
    @Override
    public ExecutionTask createTask(String taskName, List<String> caseIds) {
        ExecutionTask task = new ExecutionTask();
        task.setTaskName(taskName);
        task.setCaseIds(caseIds.toString());
        task.setTaskStatus(1); // 1-等待
        task.setTotalCases(caseIds.size());
        task.setSuccessCases(0);
        task.setFailedCases(0);
        task.setDeleted(0);
        task.setCreatedBy("system");
        task.setCreatedTime(LocalDateTime.now());
        task.setUpdatedBy("system");
        task.setUpdatedTime(LocalDateTime.now());
        
        save(task);
        return task;
    }
    
    @Override
    public ExecutionTask getTaskById(Long taskId) {
        LambdaQueryWrapper<ExecutionTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExecutionTask::getId, taskId)
                   .eq(ExecutionTask::getDeleted, 0);
        return getOne(queryWrapper);
    }
    
    @Override
    public boolean updateTaskStatus(Long taskId, Integer status) {
        ExecutionTask task = getTaskById(taskId);
        if (task != null) {
            task.setTaskStatus(status);
            if (status == 2) { // 2-执行中
                task.setStartTime(LocalDateTime.now());
            } else if (status == 3) { // 3-执行结束
                task.setEndTime(LocalDateTime.now());
            }
            task.setUpdatedBy("system");
            task.setUpdatedTime(LocalDateTime.now());
            return updateById(task);
        }
        return false;
    }
    
    @Override
    public List<ExecutionTask> getAllTasks() {
        LambdaQueryWrapper<ExecutionTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExecutionTask::getDeleted, 0)
                   .orderByDesc(ExecutionTask::getCreatedTime);
        return list(queryWrapper);
    }
}