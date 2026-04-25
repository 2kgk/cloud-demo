package com.example.execution.util;

import com.example.testcase.entity.TestCase;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

/**
 * 用例执行上下文，使用ThreadLocal存储用例的中间变量
 */
public class CaseContext {
    
    /**
     * ThreadLocal存储用例执行上下文
     */
    private static final ThreadLocal<Map<String, Object>> contextHolder = ThreadLocal.withInitial(HashMap::new);
    
    /**
     * 设置用例到当前线程上下文
     */
    public static void setCurrentCase(TestCase testCase) {
        Map<String, Object> context = contextHolder.get();
        if (context != null) {
            context.put("testCase", testCase);
            context.put("caseId", testCase.getCaseId());
            context.put("pathId", testCase.getPathId());
            context.put("projectId", testCase.getProjectId());
            
            // 设置MDC，供KafkaAppender使用
            MDC.put("caseId", testCase.getCaseId());
            MDC.put("pathId", testCase.getPathId());
            MDC.put("caseName", testCase.getCaseName());
        }
    }
    
    /**
     * 从当前线程上下文获取用例
     */
    public static TestCase getCurrentCase() {
        Map<String, Object> context = contextHolder.get();
        if (context != null) {
            return (TestCase) context.get("testCase");
        }
        return null;
    }
    
    /**
     * 从当前线程上下文获取用例ID
     */
    public static String getCurrentCaseId() {
        Map<String, Object> context = contextHolder.get();
        if (context != null) {
            return (String) context.get("caseId");
        }
        return null;
    }
    
    /**
     * 从当前线程上下文获取路径ID
     */
    public static String getCurrentPathId() {
        Map<String, Object> context = contextHolder.get();
        if (context != null) {
            return (String) context.get("pathId");
        }
        return null;
    }
    
    /**
     * 从当前线程上下文获取项目ID
     */
    public static Long getCurrentProjectId() {
        Map<String, Object> context = contextHolder.get();
        if (context != null) {
            return (Long) context.get("projectId");
        }
        return null;
    }
    
    /**
     * 向当前线程上下文添加中间变量
     */
    public static void putVariable(String key, Object value) {
        Map<String, Object> context = contextHolder.get();
        if (context != null) {
            context.put(key, value);
        }
    }
    
    /**
     * 从当前线程上下文获取中间变量
     */
    public static Object getVariable(String key) {
        Map<String, Object> context = contextHolder.get();
        if (context != null) {
            return context.get(key);
        }
        return null;
    }
    
    /**
     * 清除当前线程上下文
     */
    public static void clear() {
        contextHolder.remove();
        MDC.clear(); // 清除MDC
    }
    
    /**
     * 获取当前线程上下文的所有变量
     */
    public static Map<String, Object> getAllVariables() {
        return new HashMap<>(contextHolder.get());
    }
}