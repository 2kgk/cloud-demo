package com.example.execution.service;

import com.example.execution.feign.CaseServiceFeign;
import com.example.execution.util.CaseContext;
import com.example.testcase.entity.TestCase;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * 执行服务，负责定时拉取case服务内的案例进行并发执行
 */
@Service
public class ExecutionService {
    
    private static final Logger log = LoggerFactory.getLogger(ExecutionService.class);
    
    @Autowired
    private CaseServiceFeign caseServiceFeign;
    
    // 线程池配置
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            4, // 核心线程数
            8, // 最大线程数
            60L, // 空闲线程存活时间
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(4), // 任务队列
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
    );
    
    // 系统资源阈值
    private static final double CPU_USAGE_THRESHOLD = 0.8;   // CPU使用率阈值 80%
    private static final double MEMORY_USAGE_THRESHOLD = 0.8; // 内存使用率阈值 80%

    /**
     * 定时任务：每1秒执行一次，执行前检查系统负载和线程池资源
     */
    @Scheduled(fixedRate = 1000) // 1秒执行一次
    public void executeCasesScheduled() {
        // 检查系统负载
        if (!checkSystemResource()) {
            log.warn("系统负载过高，跳过本次任务 - {}", LocalDateTime.now());
            return;
        }

        // 检查线程池是否有空闲线程
        if (!checkThreadPoolAvailable()) {
            log.warn("线程池资源不足，跳过本次任务 - {}", LocalDateTime.now());
            return;
        }

        log.info("开始执行定时任务 - {}", LocalDateTime.now());

        try {
            // 通过Feign客户端拉取所有未删除的用例
            List<TestCase> allCases = caseServiceFeign.getAllActiveCases(2);

            log.info("通过Feign获取到 {} 个活跃用例，开始并发执行", allCases.size());

            if (allCases.isEmpty()) {
                log.info("没有找到活跃用例，跳过执行");
                return;
            }

            // 并发执行用例
            executeCasesConcurrently(allCases);

        } catch (Exception e) {
            log.error("定时任务执行失败 - {}", e.getMessage(), e);
        }
    }

    /**
     * 检查系统CPU和内存使用率
     */
    private boolean checkSystemResource() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double cpuUsage = osBean.getCpuLoad();
        double freeMemory = osBean.getFreeMemorySize();
        double totalMemory = osBean.getTotalMemorySize();
        double memoryUsage = 1.0 - (freeMemory / totalMemory);

        log.debug("系统资源 - CPU使用率: {}, 内存使用率: {}", String.format("%.2f", cpuUsage), String.format("%.2f", memoryUsage));

        if (cpuUsage > CPU_USAGE_THRESHOLD) {
            log.warn("CPU使用率过高 - 当前: {}, 阈值: {}", String.format("%.2f", cpuUsage), CPU_USAGE_THRESHOLD);
            return false;
        }
        if (memoryUsage > MEMORY_USAGE_THRESHOLD) {
            log.warn("内存使用率过高 - 当前: {}, 阈值: {}", String.format("%.2f", memoryUsage), MEMORY_USAGE_THRESHOLD);
            return false;
        }
        return true;
    }

    /**
     * 检查线程池是否有空闲资源
     */
    private boolean checkThreadPoolAvailable() {
        int activeCount = executor.getActiveCount();
        int maximumPoolSize = executor.getMaximumPoolSize();
        int queueSize = executor.getQueue().size();
        int queueRemaining = executor.getQueue().remainingCapacity();

        log.debug("线程池状态 - 活跃线程: {}, 最大线程: {}, 队列剩余: {}", activeCount, maximumPoolSize, queueRemaining);

        // 活跃线程数达到最大 或 队列已满，视为无可用资源
        if (activeCount >= maximumPoolSize && queueRemaining <= 0) {
            log.warn("线程池无空闲资源 - 活跃线程: {}, 队列已满: {}", activeCount, queueSize);
            return false;
        }
        return true;
    }
    
    /**
     * 并发执行用例
     */
    public void executeCasesConcurrently(List<TestCase> cases) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        // 为每个用例创建一个异步任务
        for (TestCase testCase : cases) {
            final TestCase currentCase = testCase;
            
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    executeSingleCase(currentCase);
                } catch (Exception e) {
                    log.error("执行用例失败 - CaseId: {}, PathId: {}, Error: {}", 
                            currentCase.getCaseId(), currentCase.getPathId(), e.getMessage(), e);
                }
            }, executor);
            
            futures.add(future);
        }
        
        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        
        try {
            // 设置超时时间为10分钟
            allFutures.get(10, TimeUnit.MINUTES);
            log.info("所有用例执行完成 - 总数量: {}", cases.size());
        } catch (TimeoutException e) {
            log.error("用例执行超时 - 超时时间: 10分钟");
        } catch (Exception e) {
            log.error("等待用例完成时发生异常 - {}", e.getMessage(), e);
        }
    }
    
    /**
     * 执行单个用例
     */
    private void executeSingleCase(TestCase testCase) {
        // 设置当前线程的用例上下文
        CaseContext.setCurrentCase(testCase);
        
        String threadName = Thread.currentThread().getName();
        log.info("开始执行用例 - 线程: {}, CaseId: {}, PathId: {}, CaseName: {}", 
                threadName, testCase.getCaseId(), testCase.getPathId(), testCase.getCaseName());
        
        try {
            // 在ThreadLocal中设置开始时间
            CaseContext.putVariable("startTime", LocalDateTime.now());
            
            // 执行测试逻辑（使用线程睡眠一分钟代替）
            log.info("开始执行测试逻辑 - CaseId: {}, 将模拟执行1分钟", testCase.getCaseId());
            log.info("开始执行步骤1");
            TimeUnit.SECONDS.sleep(10);
            log.info("步骤1执行完成");
            log.info("开始执行步骤2");
            TimeUnit.SECONDS.sleep(10);
            log.info("步骤2执行完成");
            log.info("开始执行步骤3");
            TimeUnit.SECONDS.sleep(10);
            log.info("步骤3执行完成");
            log.info("开始执行步骤4");
            TimeUnit.SECONDS.sleep(10);
            log.info("步骤4执行完成");
            TimeUnit.SECONDS.sleep(10); // 线程睡眠一分钟，模拟执行逻辑
            
            // 在ThreadLocal中设置结束时间
            CaseContext.putVariable("endTime", LocalDateTime.now());
            
            // 从ThreadLocal中获取执行时间
            LocalDateTime startTime = (LocalDateTime) CaseContext.getVariable("startTime");
            LocalDateTime endTime = (LocalDateTime) CaseContext.getVariable("endTime");
            
            if (startTime != null && endTime != null) {
                long duration = java.time.Duration.between(startTime, endTime).toMillis();
                log.info("用例执行完成 - CaseId: {}, 执行时间: {}ms", testCase.getCaseId(), duration);
            }
            
            // 在ThreadLocal中保存执行结果信息
            CaseContext.putVariable("executionStatus", "SUCCESS");
            CaseContext.putVariable("executionMessage", "执行成功");
            
        } catch (InterruptedException e) {
            log.error("用例执行被中断 - CaseId: {}, PathId: {}, Error: {}", 
                    testCase.getCaseId(), testCase.getPathId(), e.getMessage());
            Thread.currentThread().interrupt(); // 恢复中断状态
            
            // 在ThreadLocal中保存失败信息
            CaseContext.putVariable("executionStatus", "INTERRUPTED");
            CaseContext.putVariable("executionMessage", "执行被中断");
            
        } catch (Exception e) {
            log.error("用例执行异常 - CaseId: {}, PathId: {}, Error: {}", 
                    testCase.getCaseId(), testCase.getPathId(), e.getMessage(), e);
            
            // 在ThreadLocal中保存失败信息
            CaseContext.putVariable("executionStatus", "FAILED");
            CaseContext.putVariable("executionMessage", "执行异常: " + e.getMessage());
        } finally {
            // 打印当前线程的上下文信息
            Map<String, Object> context = CaseContext.getAllVariables();
            log.debug("用例执行上下文信息 - CaseId: {}, Context: {}", testCase.getCaseId(), context);
            
            // 清除当前线程的上下文
            CaseContext.clear();
        }
    }
}