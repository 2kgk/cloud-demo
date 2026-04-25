package com.example.testcase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.testcase.entity.ExecutionResult;
import com.example.testcase.mapper.ExecutionResultMapper;
import com.example.testcase.service.ExecutionResultService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 执行结果服务实现类
 */
@Service
public class ExecutionResultServiceImpl extends ServiceImpl<ExecutionResultMapper, ExecutionResult> implements ExecutionResultService {
    
    @Override
    public ExecutionResult createResult(Long taskId, String caseId, String caseTableSuffix, Integer executionResult,
                                        Integer executionTime, String errorMessage, String actualResult, Integer assertResult) {
        ExecutionResult result = new ExecutionResult();
        result.setTaskId(taskId);
        result.setCaseId(caseId);
        result.setCaseTableSuffix(caseTableSuffix);
        result.setExecutionResult(executionResult);
        result.setExecutionTime(executionTime);
        result.setErrorMessage(errorMessage);
        result.setActualResult(actualResult);
        result.setAssertResult(assertResult);
        result.setDeleted(0);
        result.setCreatedBy("system");
        result.setCreatedTime(LocalDateTime.now());
        result.setUpdatedBy("system");
        result.setUpdatedTime(LocalDateTime.now());
        
        save(result);
        return result;
    }
    
    @Override
    public List<ExecutionResult> getResultsByTaskId(Long taskId) {
        LambdaQueryWrapper<ExecutionResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExecutionResult::getTaskId, taskId)
                   .eq(ExecutionResult::getDeleted, 0)
                   .orderByDesc(ExecutionResult::getCreatedTime);
        return list(queryWrapper);
    }
    
    @Override
    public List<ExecutionResult> getResultsByCaseId(String caseId) {
        LambdaQueryWrapper<ExecutionResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExecutionResult::getCaseId, caseId)
                   .eq(ExecutionResult::getDeleted, 0)
                   .orderByDesc(ExecutionResult::getCreatedTime);
        return list(queryWrapper);
    }
    
    @Override
    public ExecutionResult getResultByTaskAndCase(Long taskId, String caseId) {
        LambdaQueryWrapper<ExecutionResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExecutionResult::getTaskId, taskId)
                   .eq(ExecutionResult::getCaseId, caseId)
                   .eq(ExecutionResult::getDeleted, 0);
        return getOne(queryWrapper);
    }
}