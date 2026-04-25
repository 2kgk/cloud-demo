package com.example.execution.service;

import com.example.execution.feign.CaseServiceFeign;
import com.example.execution.util.CaseContext;
import com.example.testcase.entity.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
            new LinkedBlockingQueue<>(100), // 任务队列
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
    );
    
    /**
     * 定时任务：每5分钟执行一次，拉取所有未删除的用例进行执行
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟执行一次
    public void executeCasesScheduled() {
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
    
    /**
     * 获取线程池状态
     */
    public void getThreadPoolStatus() {
        log.info("线程池状态 - 核心线程数: {}, 最大线程数: {}, 当前活跃线程数: {}, " +
                "已完成任务数: {}, 任务队列大小: {}",
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getActiveCount(),
                executor.getCompletedTaskCount(),
                executor.getQueue().size());
    }
    
    /**
     * 关闭线程池
     */
    public void shutdown() {
        log.info("开始关闭线程池");
        executor.shutdown();
        try {
            // 等待任务完成，最多等待1分钟
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                log.warn("线程池关闭超时，强制关闭");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("关闭线程池时发生中断", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("线程池已关闭");
    }
}