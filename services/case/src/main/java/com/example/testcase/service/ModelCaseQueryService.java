package com.example.testcase.service;

import com.example.testcase.dto.CaseInfo;
import com.example.testcase.feign.ModelServiceClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Model用例查询服务
 * 包含本地缓存功能
 */
@Service
public class ModelCaseQueryService {
    
    private static final Logger log = LoggerFactory.getLogger(ModelCaseQueryService.class);
    
    @Autowired
    private ModelServiceClient modelServiceClient;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 本地内存缓存：pathId -> CaseInfo
    private final Map<String, CaseInfo> caseCache = new ConcurrentHashMap<>();
    
    // 缓存最大容量
    private static final int MAX_CACHE_SIZE = 10000;
    
    @PostConstruct
    public void init() {
        log.info("Model用例查询服务初始化完成，本地缓存容量: {}", MAX_CACHE_SIZE);
    }
    
    /**
     * 批量查询用例信息（带缓存）
     * 
     * @param pathIds 路径ID列表
     * @return 用例信息列表（pathId -> CaseInfo）
     */
    public Map<String, CaseInfo> batchQueryCasesWithCache(List<Integer> pathIds) {
        if (pathIds == null || pathIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<String, CaseInfo> result = new HashMap<>();
        List<Integer> needQueryPathIds = new ArrayList<>();
        
        // 先从缓存中查找
        for (Integer pathId : pathIds) {
            String key = String.valueOf(pathId);
            CaseInfo cached = caseCache.get(key);
            if (cached != null) {
                result.put(key, cached);
                log.debug("从缓存中获取用例信息 - PathId: {}", key);
            } else {
                needQueryPathIds.add(pathId);
            }
        }
        
        // 批量查询未命中的数据
        if (!needQueryPathIds.isEmpty()) {
            try {
                Map<String, Object> response = modelServiceClient.batchQueryCases(needQueryPathIds);
                
                if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                    Object data = response.get("data");
                    if (data != null) {
                        List<CaseInfo> caseInfos = objectMapper.convertValue(
                            data, 
                            new TypeReference<List<CaseInfo>>() {}
                        );
                        
                        // 将查询结果放入缓存
                        for (CaseInfo caseInfo : caseInfos) {
                            if (caseInfo != null && caseInfo.getPathId() != null) {
                                result.put(caseInfo.getPathId(), caseInfo);
                                
                                // 放入缓存，如果超过容量则清理旧数据
                                if (caseCache.size() >= MAX_CACHE_SIZE) {
                                    clearOldestCache();
                                }
                                caseCache.put(caseInfo.getPathId(), caseInfo);
                            }
                        }
                        
                        log.info("从Model服务批量查询用例信息成功 - 查询数量: {}, 缓存命中: {}, 新查询: {}", 
                            pathIds.size(), 
                            pathIds.size() - needQueryPathIds.size(), 
                            caseInfos.size());
                    }
                }
            } catch (Exception e) {
                log.error("批量查询用例信息失败 - PathIds: {}, Error: {}", needQueryPathIds, e.getMessage(), e);
            }
        }
        
        return result;
    }
    
    /**
     * 清理最旧的缓存数据
     */
    private void clearOldestCache() {
        int clearCount = MAX_CACHE_SIZE / 10; // 清理10%的缓存
        Iterator<Map.Entry<String, CaseInfo>> iterator = caseCache.entrySet().iterator();
        int cleared = 0;
        while (iterator.hasNext() && cleared < clearCount) {
            iterator.next();
            iterator.remove();
            cleared++;
        }
        log.info("清理缓存数据 - 清理数量: {}, 剩余缓存: {}", cleared, caseCache.size());
    }
    
    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        caseCache.clear();
        log.info("清空所有缓存数据");
    }
    
    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        return caseCache.size();
    }
}