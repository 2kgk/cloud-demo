package com.example.testcase.controller;

import com.example.testcase.service.TestCaseShardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分片查询控制器
 * 用于查询 project_id 对应的分表信息
 */
@RestController
@RequestMapping("/api/sharding")
@CrossOrigin(origins = "*")
public class ShardingController {

    @Autowired
    private TestCaseShardingService testCaseShardingService;

    /**
     * 查询单个 project_id 对应的分表
     * 
     * @param projectId 项目ID
     * @return 分表信息
     */
    @GetMapping("/table/{projectId}")
    public Map<String, Object> getTableByProjectId(@PathVariable Long projectId) {
        String tableName = testCaseShardingService.getTableByProjectId(projectId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("projectId", projectId);
        result.put("tableName", tableName);
        result.put("message", "项目 " + projectId + " 的测试用例将被存储在表 " + tableName + " 中");
        
        return result;
    }

    /**
     * 批量查询多个 project_id 对应的分表
     * 
     * @param projectIds 项目ID列表（以逗号分隔）
     * @return 分表信息映射
     */
    @GetMapping("/tables")
    public Map<String, Object> batchGetTables(@RequestParam String projectIds) {
        // 解析项目ID列表
        List<Long> idList = parseProjectIds(projectIds);
        
        Map<Long, String> tableMapping = testCaseShardingService.batchGetTables(idList);
        
        Map<String, Object> result = new HashMap<>();
        result.put("projectIds", idList);
        result.put("tableMapping", tableMapping);
        result.put("message", "共查询了 " + idList.size() + " 个项目ID，映射到 " + tableMapping.size() + " 个不同的分表");
        
        return result;
    }

    /**
     * 测试分片算法
     * 
     * @param projectId 项目ID
     * @return 分片测试结果
     */
    @GetMapping("/test/{projectId}")
    public Map<String, Object> testSharding(@PathVariable Long projectId) {
        // 模拟多次哈希计算，展示虚拟节点的影响
        String baseTable = testCaseShardingService.getTableByProjectId(projectId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("projectId", projectId);
        result.put("baseTable", baseTable);
        
        // 测试不同格式的哈希值
        Map<String, String> hashTests = new HashMap<>();
        hashTests.put("数字格式", testCaseShardingService.getTableByProjectId(String.valueOf(projectId)));
        hashTests.put("字符串格式", testCaseShardingService.getTableByProjectId("project_" + projectId));
        hashTests.put("ID前缀", testCaseShardingService.getTableByProjectId("id_" + projectId));
        
        result.put("hashVariations", hashTests);
        result.put("isConsistent", hashTests.values().stream().allMatch(t -> t.equals(baseTable)));
        
        return result;
    }

    /**
     * 获取所有可用的分表列表
     * 
     * @return 分表列表
     */
    @GetMapping("/tables/available")
    public Map<String, Object> getAvailableTables() {
        // 这里我们模拟调用内部方法，实际项目中可以从配置读取
        List<String> tables = testCaseShardingService.getAvailableTables();
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalTables", tables.size());
        result.put("tables", tables);
        
        return result;
    }

    /**
     * 解析项目ID列表
     */
    private List<Long> parseProjectIds(String projectIdsStr) {
        String[] ids = projectIdsStr.split(",");
        List<Long> result = new java.util.ArrayList<>();
        
        for (String idStr : ids) {
            try {
                Long id = Long.parseLong(idStr.trim());
                result.add(id);
            } catch (NumberFormatException e) {
                // 忽略无效的ID
            }
        }
        
        return result;
    }
}