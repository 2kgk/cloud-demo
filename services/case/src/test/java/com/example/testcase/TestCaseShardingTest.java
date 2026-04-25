package com.example.testcase;

import com.example.testcase.entity.TestCase;
import com.example.testcase.service.CaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TestCase分表功能测试类
 */
@SpringBootTest
public class TestCaseShardingTest {

    @Autowired
    private CaseService caseService;

    /**
     * 测试插入数据到不同分表
     */
    @Test
    public void testInsertToDifferentShards() {
        // 创建测试数据 - 不同projectId应该分布到不同分表
        TestCase testCase1 = new TestCase();
        testCase1.setPathId("path1");
        testCase1.setCaseId("case001");
        testCase1.setProjectId(1L);  // 应该分到 case_00
        testCase1.setCaseName("测试用例1");
        testCase1.setCaseDesc("测试用例描述1");
        testCase1.setRequestData("{}");
        testCase1.setExpectedResult("success");
        testCase1.setCaseType(1);
        testCase1.setPriority(1);
        testCase1.setDeleted(0);
        testCase1.setCreatedBy("test");
        testCase1.setCreatedTime(LocalDateTime.now());
        
        TestCase testCase2 = new TestCase();
        testCase2.setPathId("path2");
        testCase2.setCaseId("case002");
        testCase2.setProjectId(32L); // 应该分到 case_31 (32 % 32 = 0, case_00)
        testCase2.setCaseName("测试用例2");
        testCase2.setCaseDesc("测试用例描述2");
        testCase2.setRequestData("{}");
        testCase2.setExpectedResult("success");
        testCase2.setCaseType(1);
        testCase2.setPriority(1);
        testCase2.setDeleted(0);
        testCase2.setCreatedBy("test");
        testCase2.setCreatedTime(LocalDateTime.now());
        
        TestCase testCase3 = new TestCase();
        testCase3.setPathId("path3");
        testCase3.setCaseId("case003");
        testCase3.setProjectId(33L); // 应该分到 case_01 (33 % 32 = 1, case_01)
        testCase3.setCaseName("测试用例3");
        testCase3.setCaseDesc("测试用例描述3");
        testCase3.setRequestData("{}");
        testCase3.setExpectedResult("success");
        testCase3.setCaseType(1);
        testCase3.setPriority(1);
        testCase3.setDeleted(0);
        testCase3.setCreatedBy("test");
        testCase3.setCreatedTime(LocalDateTime.now());
        
        // 保存数据
        caseService.save(testCase1);
        caseService.save(testCase2);
        caseService.save(testCase3);
        
        System.out.println("测试数据插入成功");
    }
    
    /**
     * 测试根据projectId查询数据
     */
    @Test
    public void testQueryByProjectId() {
        // 查询projectId为1的所有用例
        List<TestCase> cases1 = caseService.getCasesByProjectId(1L);
        System.out.println("projectId=1 的用例数量: " + cases1.size());
        
        // 查询projectId为32的所有用例
        List<TestCase> cases2 = caseService.getCasesByProjectId(32L);
        System.out.println("projectId=32 的用例数量: " + cases2.size());
        
        // 查询projectId为33的所有用例
        List<TestCase> cases3 = caseService.getCasesByProjectId(33L);
        System.out.println("projectId=33 的用例数量: " + cases3.size());
    }
    
    /**
     * 测试根据caseId查询数据
     */
    @Test
    public void testQueryByCaseId() {
        // 根据caseId查询用例
        TestCase testCase = caseService.getCaseById("case001");
        System.out.println("查询到的用例: " + (testCase != null ? testCase.getCaseName() : "null"));
    }
}