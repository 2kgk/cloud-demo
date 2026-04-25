package com.example.testcase.controller;

import com.example.testcase.entity.ExecutionTask;
import com.example.testcase.service.ExecutionTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 执行任务控制器
 */
@RestController
@RequestMapping("/api/tasks")
public class ExecutionTaskController {
    
    @Autowired
    private ExecutionTaskService executionTaskService;
    
    /**
     * 创建执行任务
     */
    @PostMapping
    public ExecutionTask createTask(@RequestBody CreateTaskRequest request) {
        return executionTaskService.createTask(request.getTaskName(), request.getCaseIds());
    }
    
    /**
     * 根据任务ID查询任务
     */
    @GetMapping("/{taskId}")
    public ExecutionTask getTaskById(@PathVariable Long taskId) {
        return executionTaskService.getTaskById(taskId);
    }
    
    /**
     * 更新任务状态
     */
    @PutMapping("/{taskId}/status")
    public boolean updateTaskStatus(@PathVariable Long taskId, @RequestBody StatusUpdateRequest request) {
        return executionTaskService.updateTaskStatus(taskId, request.getStatus());
    }
    
    /**
     * 查询所有任务
     */
    @GetMapping
    public List<ExecutionTask> getAllTasks() {
        return executionTaskService.getAllTasks();
    }
    
    /**
     * 创建任务请求
     */
    public static class CreateTaskRequest {
        private String taskName;
        private List<String> caseIds;
        
        public String getTaskName() {
            return taskName;
        }
        
        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }
        
        public List<String> getCaseIds() {
            return caseIds;
        }
        
        public void setCaseIds(List<String> caseIds) {
            this.caseIds = caseIds;
        }
    }
    
    /**
     * 状态更新请求
     */
    public static class StatusUpdateRequest {
        private Integer status;
        
        public Integer getStatus() {
            return status;
        }
        
        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}