package com.example.testcase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.testcase.entity.TestCase;

import java.util.List;

/**
 * 用例服务接口
 */
public interface CaseService extends IService<TestCase> {
    
    /**
     * 根据项目ID查询用例
     */
    List<TestCase> getCasesByProjectId(Long projectId);
    
    /**
     * 根据用例ID查询用例
     */
    TestCase getCaseById(String caseId);
    
    /**
     * 根据路径ID列表批量查询用例
     */
    List<TestCase> getCasesByPathIds(List<String> pathIds);
    
    /**
     * 批量保存测试用例
     */
    boolean batchSaveCases(List<TestCase> testCases);
}
