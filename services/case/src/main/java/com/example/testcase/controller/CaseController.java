package com.example.testcase.controller;

import com.example.testcase.entity.Project;
import com.example.testcase.entity.TestCase;
import com.example.testcase.service.CaseService;
import com.example.testcase.service.ProjectService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 用例控制器
 */
@RestController
@RequestMapping("/api/cases")
public class CaseController {
    
    @Resource
    private CaseService caseService;

    @Resource
    private ProjectService projectService;
    
    /**
     * 根据项目ID查询用例列表
     */
    @GetMapping("/project/{projectId}")
    public List<TestCase> getCasesByProjectId(@PathVariable Long projectId) {
        return caseService.getCasesByProjectId(projectId);
    }
    
    /**
     * 根据用例ID查询用例
     */
    @GetMapping("/{caseId}")
    public TestCase getCaseById(@PathVariable String caseId) {
        return caseService.getCaseById(caseId);
    }

    @GetMapping("/test")
    public String test() {
        // 检查是否已存在项目数据
        if (projectService.count() == 0) {
            // 创建测试项目
            Project project1 = new Project();
            project1.setProjectName("测试项目1");
            project1.setDeleted(0);
            project1.setCreatedBy("system");
            projectService.save(project1);

            Project project2 = new Project();
            project2.setProjectName("测试项目2");
            project2.setDeleted(0);
            project2.setCreatedBy("system");
            projectService.save(project2);

            System.out.println("项目数据初始化完成");
        }
        // 检查是否已存在用例数据
        // 获取项目
        List<Project> projects = projectService.list();

        Project project1 = projects.get(0);
        Project project2 = projects.get(1);

        // 创建测试用例
        List<TestCase> cases1 = Arrays.asList(
                createTestCase("CASE001-001", project1.getId(), "登录接口测试", "验证用户登录功能",
                        "{\"username\":\"test1\",\"password\":\"123456\"}", "{\"code\":200,\"message\":\"登录成功\"}"),
                createTestCase("CASE001-002", project1.getId(), "获取用户信息", "验证获取用户信息功能",
                        "{\"userId\":\"1\"}", "{\"code\":200,\"data\":{\"username\":\"test1\"}}"),
                createTestCase("CASE001-003", project1.getId(), "修改密码", "验证修改密码功能",
                        "{\"oldPassword\":\"123456\",\"newPassword\":\"654321\"}", "{\"code\":200,\"message\":\"密码修改成功\"}")
        );

        List<TestCase> cases2 = Arrays.asList(
                createTestCase("CASE002-001", project2.getId(), "商品查询", "验证商品查询功能",
                        "{\"categoryId\":\"1\",\"page\":1,\"size\":10}", "{\"code\":200,\"data\":[]}"),
                createTestCase("CASE002-002", project2.getId(), "添加购物车", "验证添加购物车功能",
                        "{\"productId\":\"1\",\"quantity\":1}", "{\"code\":200,\"message\":\"添加成功\"}")
        );

        caseService.saveBatch(cases1);
        caseService.saveBatch(cases2);

        System.out.println("用例数据初始化完成");
        return "Hello World!";
    }

    private TestCase createTestCase(String caseId, Long projectId, String caseName, String caseDesc,
                                    String requestData, String expectedResult) {
        TestCase testCase = new TestCase();
        testCase.setCaseId(caseId);
        testCase.setProjectId(projectId);
        testCase.setCaseName(caseName);
        testCase.setCaseDesc(caseDesc);
        testCase.setRequestData(requestData);
        testCase.setExpectedResult(expectedResult);
        testCase.setCaseType(1); // 1-接口
        testCase.setPriority(1); // 1-高
        testCase.setDeleted(0);
        testCase.setCreatedBy("system");
        testCase.setPathId("33333");
        return testCase;
    }
}