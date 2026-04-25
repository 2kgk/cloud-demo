package com.example.testcase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.testcase.entity.ExecutionResult;

import java.util.List;

/**
 * 执行结果服务接口
 */
public interface ExecutionResultService extends IService<ExecutionResult> {
    
    /**
     * 创建执行结果
     */
    ExecutionResult createResult(Long taskId, String caseId, String caseTableSuffix, Integer executionResult, 
                                Integer executionTime, String errorMessage, String actualResult, Integer assertResult);
    
    /**
     * 根据任务ID查询执行结果
     */
    List<ExecutionResult> getResultsByTaskId(Long taskId);
    
    /**
     * 根据用例ID查询执行结果
     */
    List<ExecutionResult> getResultsByCaseId(String caseId);
    
    /**
     * 根据任务ID和用例ID查询执行结果
     */
    ExecutionResult getResultByTaskAndCase(Long taskId, String caseId);
}