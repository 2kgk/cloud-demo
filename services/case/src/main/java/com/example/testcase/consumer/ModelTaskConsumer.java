package com.example.testcase.consumer;

import com.alibaba.fastjson2.JSON;
import com.example.testcase.dto.CaseInfo;
import com.example.testcase.entity.ModelBatchProgress;
import com.example.testcase.entity.TestCase;
import com.example.testcase.service.CaseService;
import com.example.testcase.service.ModelBatchProgressService;
import com.example.testcase.service.ModelCaseQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 模型任务消费者
 * 监听test-topic主题，消费模型任务消息
 */
@Component
public class ModelTaskConsumer {

    private static final Logger log = LoggerFactory.getLogger(ModelTaskConsumer.class);

    @Autowired
    private ModelBatchProgressService modelBatchProgressService;

    @Autowired
    private ModelCaseQueryService modelCaseQueryService;

    @Autowired
    private CaseService caseService;

    // 线程池配置
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            4, // 核心线程数
            8, // 最大线程数
            60L, // 空闲线程存活时间
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(600), // 任务队列
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
    );

    /**
     * 监听test-topic主题，消费模型任务消息
     *
     * @param message   消息内容（JSON字符串）
     * @param topic     主题
     * @param partition 分区
     * @param offset    偏移量
     * @param key       消息键
     * @param ack       手动确认对象
     */
    @KafkaListener(
            topics = "${kafka.topic.model-task:test-topic}",
            groupId = "${spring.kafka.consumer.group-id:case-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeModelTask(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) String partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        String taskId = null;
        Integer batchIndex = null;

        try {
            log.info("收到模型任务消息 - Topic: {}, Partition: {}, Offset: {}, Key: {}, Message: {}",
                    topic, partition, offset, key, message);

            // 解析JSON消息
            Map<String, Object> taskMessage = JSON.parseObject(message, Map.class);

            // 提取关键信息
            taskId = (String) taskMessage.get("taskId");
            Long modelId = getLongValue(taskMessage, "modelId");
            Long suiteId = getLongValue(taskMessage, "suiteId");
            Long userId = getLongValue(taskMessage, "userId");
            Integer totalBatchCount = getIntegerValue(taskMessage, "totalBatchCount");
            batchIndex = getIntegerValue(taskMessage, "batchIndex");

            log.info("任务详情 - TaskId: {}, ModelId: {}, SuiteId: {}, UserId: {}, Batch: {}/{}, PathIds数量: {}, CaseIds数量: {}",
                    taskId, modelId, suiteId, userId, batchIndex, totalBatchCount,
                    taskMessage.get("pathIds") != null ? ((java.util.List<?>) taskMessage.get("pathIds")).size() : 0,
                    taskMessage.get("caseIds") != null ? ((java.util.List<?>) taskMessage.get("caseIds")).size() : 0);

            // 幂等性检查：如果该批次已经处理过，则直接返回
            if (modelBatchProgressService.isBatchProcessed(taskId, batchIndex)) {
                log.warn("批次已处理，跳过消费 - TaskId: {}, BatchIndex: {} (支持乱序消费)", taskId, batchIndex);
                if (ack != null) {
                    ack.acknowledge();
                }
                return;
            }

            // 处理模型任务
            processModelTask(taskMessage);

            // 手动确认消息消费成功
            if (ack != null) {
                ack.acknowledge();
                log.info("消息消费确认成功 - TaskId: {}", taskId);
            }

        } catch (Exception e) {
            log.error("消费模型任务消息失败 - TaskId: {}, BatchIndex: {}, Topic: {}, Partition: {}, Offset: {}, Error: {}",
                    taskId, batchIndex, topic, partition, offset, e.getMessage(), e);
            // 可以选择不确认消息，让Kafka重新投递
            // 或者进行其他异常处理逻辑
        }
    }

    /**
     * 处理模型任务
     * 1. 初始化或更新批次进度
     * 2. 根据pathIds批量查询用例信息
     * 3. 使用ThreadPoolExecutor+CompletableFuture并发生成testcase
     * 4. 批量保存testcase到数据库
     * 5. 更新批次进度
     * 6. 检查是否所有批次都完成，如果完成则标记任务为完成状态
     */
    private void processModelTask(Map<String, Object> taskMessage) {
        log.info("开始处理模型任务 - TaskId: {}", taskMessage.get("taskId"));

        // 提取关键参数
        String taskId = (String) taskMessage.get("taskId");
        Long modelId = getLongValue(taskMessage, "modelId");
        Long suiteId = getLongValue(taskMessage, "suiteId");
        Long userId = getLongValue(taskMessage, "userId");
        Integer totalBatchCount = getIntegerValue(taskMessage, "totalBatchCount");
        Integer batchIndex = getIntegerValue(taskMessage, "batchIndex");
        java.util.List<Integer> pathIds = (java.util.List<Integer>) taskMessage.get("pathIds");
        java.util.List<Integer> caseIds = (java.util.List<Integer>) taskMessage.get("caseIds");

        // 1. 初始化或更新批次进度
        ModelBatchProgress progress = modelBatchProgressService.getByTaskId(taskId);
        if (progress == null) {
            // 第一次处理该任务，初始化进度
            progress = modelBatchProgressService.initBatchProgress(taskId, modelId, suiteId, totalBatchCount);
            log.info("初始化批次进度 - TaskId: {}, TotalBatchCount: {}", taskId, totalBatchCount);
        }

        // 2. 根据pathIds批量查询用例信息（使用缓存）
        log.info("开始批量查询用例信息 - TaskId: {}, PathIds数量: {}", taskId, pathIds.size());
        Map<String, CaseInfo> caseInfoMap = modelCaseQueryService.batchQueryCasesWithCache(pathIds);
        log.info("批量查询用例信息完成 - TaskId: {}, 查询结果数量: {}", taskId, caseInfoMap.size());

        // 3. 使用ThreadPoolExecutor+CompletableFuture并发生成testcase（每个任务独立超时）
        int perTaskTimeoutSeconds = 30;
        List<CompletableFuture<TaskResult>> futures = new ArrayList<>();

        for (Integer pathId : pathIds) {
            final Integer currentPathId = pathId;
            final String currentTaskId = taskId;
            final Long currentModelId = modelId;
            final Long currentSuiteId = suiteId;
            final Long currentUserId = userId;

            CompletableFuture<TaskResult> future = CompletableFuture.supplyAsync(() ->
                    generateTestCase(currentPathId, caseInfoMap, currentTaskId, currentModelId, currentSuiteId, currentUserId),
                    executor
            ).orTimeout(perTaskTimeoutSeconds, TimeUnit.SECONDS)
             .handle((tc, throwable) -> {
                 if (throwable == null) {
                     return new TaskResult(tc, currentPathId, null, false);
                 }
                 boolean isTimeout = throwable instanceof TimeoutException;
                 return new TaskResult(null, currentPathId, isTimeout ? "timeout" : throwable.getMessage(), isTimeout);
             });
            futures.add(future);
        }

        // 等待所有任务完成（orTimeout保证不会永久阻塞）
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 收集结果
        List<TaskResult> taskResults = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // 收集成功的测试用例
        List<TestCase> testCases = taskResults.stream()
                .filter(TaskResult::isSuccess)
                .map(r -> r.testCase)
                .collect(Collectors.toList());

        // 记录失败的任务
        List<TaskResult> failedTasks = taskResults.stream()
                .filter(r -> !r.isSuccess())
                .collect(Collectors.toList());
        if (!failedTasks.isEmpty()) {
            log.warn("部分测试用例生成失败 - TaskId: {}, 失败数量: {}, 详情: {}",
                    taskId, failedTasks.size(),
                    failedTasks.stream().map(r -> "PathId:" + r.pathId + ", Error:" + (r.timeout ? "TIMEOUT:" : "") + r.error).collect(Collectors.joining("; ")));
        }

        log.info("测试用例生成完成 - TaskId: {}, 生成数量: {}", taskId, testCases.size());

        // 4. 批量保存testcase到数据库
        if (!testCases.isEmpty()) {
            boolean saved = caseService.batchSaveCases(testCases);
            if (saved) {
                log.info("批量保存测试用例成功 - TaskId: {}, 保存数量: {}", taskId, testCases.size());
            } else {
                log.error("批量保存测试用例失败 - TaskId: {}", taskId);
                throw new RuntimeException("批量保存测试用例失败");
            }
        }

        // 5. 更新批次进度（支持乱序消费）
        boolean updated = modelBatchProgressService.updateProcessedBatch(taskId, batchIndex);
        if (updated) {
            ModelBatchProgress latestProgress = modelBatchProgressService.getByTaskId(taskId);
            log.info("更新批次进度成功 - TaskId: {}, CurrentBatchIndex: {}/{}, ProcessedCount: {}/{}",
                    taskId, batchIndex, totalBatchCount,
                    latestProgress != null ? latestProgress.getProcessedBatchCount() : 0,
                    totalBatchCount);
        } else {
            log.error("更新批次进度失败 - TaskId: {}, BatchIndex: {}", taskId, batchIndex);
        }

        // 6. 检查是否所有批次都完成
        ModelBatchProgress updatedProgress = modelBatchProgressService.getByTaskId(taskId);
        if (updatedProgress != null && updatedProgress.getProcessedBatchCount() != null &&
                updatedProgress.getProcessedBatchIndexes().split(",").length >= totalBatchCount) {

            // 标记任务为完成状态
            boolean completed = modelBatchProgressService.markAsCompleted(taskId);
            if (completed) {
                log.info("批次任务全部完成，已标记为完成状态 - TaskId: {}, TotalBatchCount: {}, ProcessedBatchCount: {}",
                        taskId, totalBatchCount, updatedProgress.getProcessedBatchCount());
            } else {
                log.error("标记任务完成状态失败 - TaskId: {}", taskId);
            }
        } else {
            log.info("批次处理完成，等待其他批次 - TaskId: {}, ProcessedBatchCount: {}, TotalBatchCount: {}",
                    taskId, updatedProgress != null ? updatedProgress.getProcessedBatchCount() : 0, totalBatchCount);
        }
    }

    /**
     * 单个任务处理结果包装
     */
    static class TaskResult {
        final TestCase testCase;
        final Integer pathId;
        final String error;
        final boolean timeout;

        TaskResult(TestCase testCase, Integer pathId, String error, boolean timeout) {
            this.testCase = testCase;
            this.pathId = pathId;
            this.error = error;
            this.timeout = timeout;
        }

        boolean isSuccess() {
            return testCase != null;
        }
    }

    /**
     * 生成单个测试用例
     */
    private TestCase generateTestCase(Integer pathId, Map<String, CaseInfo> caseInfoMap,
                                      String taskId, Long modelId, Long suiteId, Long userId) {
        String pathIdStr = String.valueOf(pathId);
        CaseInfo caseInfo = caseInfoMap.get(pathIdStr);

        TestCase testCase = new TestCase();
        testCase.setPathId(pathIdStr);

        if (caseInfo != null) {
            testCase.setCaseId(caseInfo.getCaseId());
            testCase.setProjectId(caseInfo.getProjectId());
            testCase.setCaseName(caseInfo.getCaseName());
            testCase.setCaseDesc(caseInfo.getCaseDesc());
            testCase.setRequestData(caseInfo.getRequestData());
            testCase.setExpectedResult(caseInfo.getExpectedResult());
            testCase.setCaseType(caseInfo.getCaseType());
            testCase.setPriority(caseInfo.getPriority());
        } else {
            // 如果没有查询到用例信息，设置默认值
            testCase.setCaseId("CASE_" + pathId + "_" + System.currentTimeMillis());
            testCase.setProjectId(1L);
            testCase.setCaseName("默认测试用例_" + pathId);
            testCase.setCaseDesc("路径" + pathId + "的默认测试用例");
            testCase.setCaseType(1);
            testCase.setPriority(3);
        }

        testCase.setDeleted(0);
        testCase.setCreatedBy(userId != null ? String.valueOf(userId) : "system");
        testCase.setUpdatedBy(userId != null ? String.valueOf(userId) : "system");
        testCase.setCreatedTime(LocalDateTime.now());
        testCase.setUpdatedTime(LocalDateTime.now());

        log.debug("生成测试用例 - PathId: {}, CaseId: {}, CaseName: {}",
                pathId, testCase.getCaseId(), testCase.getCaseName());

        return testCase;
    }

    /**
     * 安全地获取Long值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.warn("无法解析Long值 - Key: {}, Value: {}", key, value);
                return null;
            }
        }
        return null;
    }

    /**
     * 安全地获取Integer值
     */
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                log.warn("无法解析Integer值 - Key: {}, Value: {}", key, value);
                return null;
            }
        }
        return null;
    }
}