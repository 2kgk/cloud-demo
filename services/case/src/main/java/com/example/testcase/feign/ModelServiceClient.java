package com.example.testcase.feign;

import com.example.testcase.dto.CaseInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * Model服务Feign客户端
 */
@FeignClient(name = "model-service", url = "${model.service.url:http://localhost:7084}")
public interface ModelServiceClient {
    
    /**
     * 批量查询用例信息
     * 
     * @param pathIds 路径ID列表
     * @return 用例信息列表
     */
    @PostMapping("/api/model/cases/batch-query")
    Map<String, Object> batchQueryCases(@RequestBody List<Integer> pathIds);
}