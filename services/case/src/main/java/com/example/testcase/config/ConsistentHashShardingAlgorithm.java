package com.example.testcase.config;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义一致性哈希分片算法（支持虚拟节点）
 * 适用于 ShardingSphere 5.5.2
 * 关键特性：自动适应可用的分表个数
 */
public class ConsistentHashShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {

    private final TreeMap<Integer, String> hashRing = new TreeMap<>();
    private final Map<String, List<Integer>> nodeMap = new ConcurrentHashMap<>();
    private static final int VIRTUAL_NODES_PER_REAL_NODE = 160; // 每个真实节点对应的虚拟节点数

    @Override
    public void init(Properties props) {
        // 可通过 props 传入虚拟节点数等参数，但不传入分表个数
        // 分表个数由 ShardingSphere 运行时动态传入
    }

    /**
     * 核心：精准分片（处理 =, IN 查询）
     * 自动适应 availableTargetNames 中的实际分表个数
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames,
                             PreciseShardingValue<Comparable<?>> shardingValue) {
        // 初始化哈希环（如果为空或节点有变化）
        synchronized (hashRing) {
            if (hashRing.isEmpty() || !hashRing.values().containsAll(availableTargetNames)) {
                // 这里会自动重建哈希环，使用当前可用的分表名称
                buildHashRing(availableTargetNames);
            }
        }

        // 获取分片键值（支持 String 或 Long）
        String key = String.valueOf(shardingValue.getValue());
        int hashCode = getHash(key);

        // 在哈希环上查找第一个 >= hashCode 的节点
        Map.Entry<Integer, String> entry = hashRing.ceilingEntry(hashCode);
        String targetNode = (entry != null) ? entry.getValue() : hashRing.firstEntry().getValue();

        return targetNode;
    }

    /**
     * 构建哈希环（含虚拟节点）
     * 根据当前可用的分表名称动态构建哈希环
     */
    private void buildHashRing(Collection<String> availableTargetNames) {
        hashRing.clear();
        nodeMap.clear();

        for (String node : availableTargetNames) {
            List<Integer> virtualNodeHashes = new ArrayList<>();
            // 为每个真实节点生成 VIRTUAL_NODES_PER_REAL_NODE 个虚拟节点
            for (int i = 0; i < VIRTUAL_NODES_PER_REAL_NODE; i++) {
                String virtualNodeName = node + "&&VN" + i;
                int hash = getHash(virtualNodeName);
                hashRing.put(hash, node);
                virtualNodeHashes.add(hash);
            }
            nodeMap.put(node, virtualNodeHashes);
        }

        // 调试信息：实际构建了多少个节点的哈希环
        System.out.println("[Debug] 一致性哈希环已构建，包含 " + availableTargetNames.size() + " 个分表:");
        for (String node : availableTargetNames) {
            System.out.println("  - " + node);
        }
    }

    /**
     * 计算哈希值（使用 FNV1_32_HASH 算法，可替换为 MurmurHash）
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

    /**
     * 范围分片（处理 BETWEEN, >, < 查询）
     * 注意：一致性哈希对范围查询不友好，通常返回所有节点
     */
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames,
                                         RangeShardingValue<Comparable<?>> shardingValue) {
        // 范围查询难以精准路由，通常返回所有可用表进行全扫
        return availableTargetNames;
    }
}
