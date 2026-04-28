package com.example.execution.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.alibaba.fastjson2.JSON;
import lombok.Setter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Kafka日志Appender，拦截所有日志并发送到Kafka
 */
@Component
public class KafkaAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    
    private static final String LOG_TOPIC = "log-topic";
    // 静态设置KafkaTemplate
    @Setter
    private static KafkaTemplate<String, String> kafkaTemplate;
    private static final String serviceName = "execution";
    
    // 异步线程池
    private static final ExecutorService executorService = new ThreadPoolExecutor(
            2, 
            4, 
            60L, 
            TimeUnit.SECONDS, 
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Override
    protected void append(ILoggingEvent event) {
        try {
            // 异步发送日志
            executorService.submit(() -> sendLogToKafka(event));
        } catch (Exception e) {
            // 避免日志发送失败影响主流程
            addError("Failed to submit log to executor", e);
        }
    }
    
    private void sendLogToKafka(ILoggingEvent event) {
        if (kafkaTemplate == null) {
            return;
        }
        
        try {
            // 构建日志JSON对象
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("timestamp", LocalDateTime.ofInstant(new Date(event.getTimeStamp()).toInstant(), ZoneId.systemDefault()));
            logMap.put("level", event.getLevel().toString());
            logMap.put("loggerName", event.getLoggerName());
            logMap.put("threadName", event.getThreadName());
            logMap.put("message", event.getFormattedMessage());
            logMap.put("serviceName", serviceName);
            
            // 从MDC中获取CaseContext信息
            String caseId = event.getMDCPropertyMap().get("caseId");
            String pathId = event.getMDCPropertyMap().get("pathId");
            String caseName = event.getMDCPropertyMap().get("caseName");
            
            if (caseId != null) {
                logMap.put("caseId", caseId);
            }
            if (pathId != null) {
                logMap.put("pathId", pathId);
            }
            if (caseName != null) {
                logMap.put("caseName", caseName);
            }
            
            // 添加异常信息
            if (event.getThrowableProxy() != null) {
                logMap.put("exception", event.getThrowableProxy().getMessage());
            }
            
            String logJson = JSON.toJSONString(logMap);
            
            // 使用caseId作为key，如果为null则使用loggerName
            String key = caseId != null ? caseId : event.getLoggerName();
            
            // 异步发送到Kafka
            kafkaTemplate.send(LOG_TOPIC, key, logJson).whenComplete((result, ex) -> {
                if (ex != null) {
                    addError("Failed to send log to Kafka", ex);
                } else {
                    addInfo("Sent log to Kafka: " + logJson);
                }
            });
            
        } catch (Exception e) {
            addError("Failed to process log event", e);
        }
    }
    
    @Override
    public void stop() {
        super.stop();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}