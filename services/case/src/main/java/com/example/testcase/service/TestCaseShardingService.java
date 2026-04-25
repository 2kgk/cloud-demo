package com.example.testcase.service;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 测试用例分片查询服务
 * 用于查询给定的 project_id 会被分配到哪张分表
 */
@Service
public class TestCaseShardingService {

    // 每个真实节点对应的虚拟节点数，必须与 ConsistentHashShardingAlgorithm 中的值一致
    private static final int VIRTUAL_NODES_PER_REAL_NODE = 160;

    /**
     * 查询给定的 project_id 会被分配到哪张分表
     * 
     * @param projectId 项目ID
     * @return 分表名称（如：test_case_00, test_case_01 等）
     */
    public String getTableByProjectId(Long projectId) {
        return getTableByProjectId(String.valueOf(projectId));
    }

    /**
     * 查询给定的 project_id 会被分配到哪张分表
     * 
     * @param projectId 项目ID
     * @return 分表名称（如：test_case_00, test_case_01 等）
     */
    public String getTableByProjectId(String projectId) {
        // 获取所有可用的分表
        List<String> availableTables = getAvailableTables();
        
        // 构建哈希环
        TreeMap<Integer, String> hashRing = buildHashRing(availableTables);
        
        // 计算 projectId 的哈希值
        int hashCode = getHash(projectId);
        
        // 在哈希环上查找第一个 >= hashCode 的节点
        Map.Entry<Integer, String> entry = hashRing.ceilingEntry(hashCode);
        String targetNode = (entry != null) ? entry.getValue() : hashRing.firstEntry().getValue();
        
        return targetNode;
    }

    /**
     * 批量查询多个 project_id 对应的分表
     * 
     * @param projectIds 项目ID列表
     * @return Map<projectId, tableName>
     */
    public Map<Long, String> batchGetTables(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return new HashMap<>();
        }

        // 获取所有可用的分表
        List<String> availableTables = getAvailableTables();
        
        // 构建哈希环
        TreeMap<Integer, String> hashRing = buildHashRing(availableTables);
        
        // 批量计算
        Map<Long, String> result = new HashMap<>();
        for (Long projectId : projectIds) {
            String projectIdStr = String.valueOf(projectId);
            int hashCode = getHash(projectIdStr);
            Map.Entry<Integer, String> entry = hashRing.ceilingEntry(hashCode);
            String targetNode = (entry != null) ? entry.getValue() : hashRing.firstEntry().getValue();
            result.put(projectId, targetNode);
        }
        
        return result;
    }

    /**
     * 获取所有可用的分表
     * 根据实际配置，这里返回 32 个分表（test_case_00 到 test_case_31）
     * 实际项目中可以从配置或数据库读取
     */
    public List<String> getAvailableTables() {
        List<String> tables = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            tables.add(String.format("test_case_%02d", i));
        }
        return tables;
    }

    /**
     * 构建哈希环（含虚拟节点）
     * 与 ConsistentHashShardingAlgorithm 中的逻辑保持一致
     */
    private TreeMap<Integer, String> buildHashRing(Collection<String> availableTargetNames) {
        TreeMap<Integer, String> hashRing = new TreeMap<>();
        
        for (String node : availableTargetNames) {
            // 为每个真实节点生成 VIRTUAL_NODES_PER_REAL_NODE 个虚拟节点
            for (int i = 0; i < VIRTUAL_NODES_PER_REAL_NODE; i++) {
                String virtualNodeName = node + "&&VN" + i;
                int hash = getHash(virtualNodeName);
                hashRing.put(hash, node);
            }
        }
        
        return hashRing;
    }

    /**
     * 计算哈希值（使用 FNV1_32_HASH 算法）
     * 与 ConsistentHashShardingAlgorithm 中的逻辑保持一致
     */
    private int getHash(String key) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < key.length(); i++) {
            hash = (hash ^ key.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return hash & 0x7fffffff; // 确保为正数
    }
}