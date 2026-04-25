package com.example.testcase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.testcase.entity.TestCase;
import com.example.testcase.mapper.TestCaseMapper;
import com.example.testcase.service.CaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 用例服务实现类
 */
@Service
public class CaseServiceImpl extends ServiceImpl<TestCaseMapper, TestCase> implements CaseService {
    
    @Override
    public List<TestCase> getCasesByProjectId(Long projectId) {
        LambdaQueryWrapper<TestCase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCase::getProjectId, projectId)
                   .eq(TestCase::getDeleted, 0)
                   .orderByDesc(TestCase::getCreatedTime);
        return list(queryWrapper);
    }
    
    @Override
    public TestCase getCaseById(String caseId) {
        LambdaQueryWrapper<TestCase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCase::getCaseId, caseId)
                   .eq(TestCase::getDeleted, 0);
        return getOne(queryWrapper);
    }
    
    @Override
    public List<TestCase> getCasesByPathIds(List<String> pathIds) {
        if (pathIds == null || pathIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        LambdaQueryWrapper<TestCase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TestCase::getPathId, pathIds)
                   .eq(TestCase::getDeleted, 0);
        return list(queryWrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSaveCases(List<TestCase> testCases) {
        if (testCases == null || testCases.isEmpty()) {
            return false;
        }
        
        return saveBatch(testCases);
    }
}