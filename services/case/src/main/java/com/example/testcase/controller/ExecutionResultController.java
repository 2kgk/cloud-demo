package com.example.testcase.controller;

import com.example.testcase.entity.ExecutionResult;
import com.example.testcase.service.ExecutionResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 执行结果控制器
 */
@RestController
@RequestMapping("/api/results")
public class ExecutionResultController {
    
    @Autowired
    private ExecutionResultService executionResultService;
    
    /**
     * 创建执行结果
     */
    @PostMapping
    public ExecutionResult createResult(@RequestBody CreateResultRequest request) {
        return executionResultService.createResult(
                request.getTaskId(),
                request.getCaseId(),
                request.getCaseTableSuffix(),
                request.getExecutionResult(),
                request.getExecutionTime(),
                request.getErrorMessage(),
                request.getActualResult(),
                request.getAssertResult()
        );
    }
    
    /**
     * 根据任务ID查询执行结果
     */
    @GetMapping("/task/{taskId}")
    public List<ExecutionResult> getResultsByTaskId(@PathVariable Long taskId) {
        return executionResultService.getResultsByTaskId(taskId);
    }
    
    /**
     * 根据用例ID查询执行结果
     */
    @GetMapping("/case/{caseId}")
    public List<ExecutionResult> getResultsByCaseId(@PathVariable String caseId) {
        return executionResultService.getResultsByCaseId(caseId);
    }
    
    /**
     * 根据任务ID和用例ID查询执行结果
     */
    @GetMapping("/task/{taskId}/case/{caseId}")
    public ExecutionResult getResultByTaskAndCase(@PathVariable Long taskId, @PathVariable String caseId) {
        return executionResultService.getResultByTaskAndCase(taskId, caseId);
    }
    
    /**
     * 创建结果请求
     */
    public static class CreateResultRequest {
        private Long taskId;
        private String caseId;
        private String caseTableSuffix;
        private Integer executionResult;
        private Integer executionTime;
        private String errorMessage;
        private String actualResult;
        private Integer assertResult;
        
        public Long getTaskId() {
            return taskId;
        }
        
        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }
        
        public String getCaseId() {
            return caseId;
        }
        
        public void setCaseId(String caseId) {
            this.caseId = caseId;
        }
        
        public String getCaseTableSuffix() {
            return caseTableSuffix;
        }
        
        public void setCaseTableSuffix(String caseTableSuffix) {
            this.caseTableSuffix = caseTableSuffix;
        }
        
        public Integer getExecutionResult() {
            return executionResult;
        }
        
        public void setExecutionResult(Integer executionResult) {
            this.executionResult = executionResult;
        }
        
        public Integer getExecutionTime() {
            return executionTime;
        }
        
        public void setExecutionTime(Integer executionTime) {
            this.executionTime = executionTime;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public String getActualResult() {
            return actualResult;
        }
        
        public void setActualResult(String actualResult) {
            this.actualResult = actualResult;
        }
        
        public Integer getAssertResult() {
            return assertResult;
        }
        
        public void setAssertResult(Integer assertResult) {
            this.assertResult = assertResult;
        }
    }
}