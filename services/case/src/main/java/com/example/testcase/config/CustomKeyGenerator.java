package com.example.testcase.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.testcase.entity.IdSegment;
import com.example.testcase.mapper.IdSegmentMapper;
import com.example.testcase.util.SpringContextHolder;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.keygen.core.KeyGenerateAlgorithm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * 自定义主键生成器 - 基于数据库分段取号
 * 
 * 注意：此类通过 ShardingSphere SPI 机制加载，不是 Spring Bean，
 * 所以需要使用 SpringContextHolder 来获取 Spring 管理的 Bean
 */
public class CustomKeyGenerator implements KeyGenerateAlgorithm {

    private static final Logger logger = Logger.getLogger(CustomKeyGenerator.class.getName());

    // 默认业务类型
    private String defaultBizType = "test_case";
    
    // 默认步长
    private int defaultStep = 1000;
    
    // 本地号段缓存：bizType -> SegmentHolder
    private final Map<String, SegmentHolder> segmentCache = new ConcurrentHashMap<>();

    // 初始化方法
    @Override
    public String getType() {
        return "CUSTOM";
    }

    @Override
    public void init(Properties props) {
        if (props != null) {
            String bizType = props.getProperty("bizType");
            String step = props.getProperty("step");
            if (bizType != null && !bizType.isEmpty()) {
                this.defaultBizType = bizType;
            }
            if (step != null && !step.isEmpty()) {
                this.defaultStep = Integer.parseInt(step);
            }
        }
    }

    @Override
    public Collection<? extends Comparable<?>> generateKeys(AlgorithmSQLContext algorithmSQLContext, int count) {
        logger.info("生成主键，数量: " + count);
        
        // 获取或创建号段持有者
        SegmentHolder holder = segmentCache.computeIfAbsent(defaultBizType, k -> new SegmentHolder(defaultBizType));
        
        List<Long> keys = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            keys.add(holder.nextId());
        }
        
        return keys;
    }

    /**
     * 号段持有者 - 管理单个业务类型的本地号段
     */
    private class SegmentHolder {
        private final String bizType;
        private final AtomicLong currentId;
        private volatile long maxId;
        private volatile int step;
        
        public SegmentHolder(String bizType) {
            this.bizType = bizType;
            this.currentId = new AtomicLong(0);
            this.maxId = 0;
            this.step = defaultStep;
            // 初始化时加载号段
            loadSegment();
        }

        /**
         * 获取下一个ID
         */
        public long nextId() {
            long id = currentId.incrementAndGet();
            
            // 如果当前ID超过最大ID，需要从数据库获取新号段
            while (id > maxId) {
                synchronized (this) {
                    // 双重检查，避免并发时重复获取
                    if (id <= maxId) {
                        return id;
                    }
                    loadSegment();
                    id = currentId.incrementAndGet();
                }
            }
            
            return id;
        }

        /**
         * 从数据库加载新号段
         */
        private void loadSegment() {
            int maxRetries = 3;
            int retryCount = 0;
            
            while (retryCount < maxRetries) {
                IdSegmentMapper idSegmentMapper = null;
                try {
                    // 从 Spring 容器获取 IdSegmentMapper
                    idSegmentMapper = SpringContextHolder.getBean(IdSegmentMapper.class);
                    
                    // 1. 查询当前号段
                    LambdaQueryWrapper<IdSegment> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(IdSegment::getBizType, bizType);
                    IdSegment segment = idSegmentMapper.selectOne(queryWrapper);
                    
                    if (segment == null) {
                        throw new RuntimeException("业务类型不存在: " + bizType);
                    }

                    long oldMaxId = segment.getMaxId();
                    int oldVersion = segment.getVersion();
                    long newMaxId = oldMaxId + segment.getStep();
                    
                    // 2. 使用乐观锁更新号段
                    int affectedRows = idSegmentMapper.updateSegment(bizType, oldMaxId, newMaxId, oldVersion);
                    
                    if (affectedRows > 0) {
                        // 更新成功，设置本地号段
                        this.currentId.set(oldMaxId + 1);
                        this.maxId = newMaxId;
                        this.step = segment.getStep();
                        logger.info(String.format("成功获取号段 - 业务类型: %s, 范围: [%d, %d], 步长: %d", 
                            bizType, oldMaxId + 1, newMaxId, segment.getStep()));
                        return;
                    } else {
                        // 更新失败，重试
                        retryCount++;
                        logger.warning("乐观锁更新失败，重试次数: " + retryCount);
                        if (retryCount < maxRetries) {
                            Thread.sleep(50); // 短暂等待后重试
                        }
                    }
                } catch (Exception e) {
                    retryCount++;
                    logger.severe("获取号段失败，重试次数: " + retryCount + ", 错误: " + e.getMessage());
                    if (retryCount < maxRetries) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("获取号段被中断", ie);
                        }
                    } else {
                        throw new RuntimeException("获取号段失败，已达到最大重试次数", e);
                    }
                }
            }
            
            throw new RuntimeException("获取号段失败，已达到最大重试次数");
        }
    }
}
