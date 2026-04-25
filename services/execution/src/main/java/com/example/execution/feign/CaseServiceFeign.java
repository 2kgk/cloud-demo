package com.example.execution.feign;

import com.example.testcase.entity.TestCase;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 用例服务Feign客户端
 */
@FeignClient(name = "case", url = "${case.service.url:http://localhost:7082}")
public interface CaseServiceFeign {
    
    /**
     * 获取所有未删除的用例
     */
    @GetMapping("/api/cases/project/{projectId}")
    List<TestCase> getAllActiveCases(@PathVariable Integer projectId);
}