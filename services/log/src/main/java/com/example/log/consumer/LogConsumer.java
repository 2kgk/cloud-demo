package com.example.log.consumer;

import com.alibaba.fastjson2.JSON;
import com.example.log.entity.LogEntry;
import com.example.log.repository.LogEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 日志消费者
 */
@Component
public class LogConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(LogConsumer.class);
    
    @Autowired
    private LogEntryRepository logEntryRepository;
    
    /**
     * 消费日志消息
     * @param message 日志JSON字符串
     * @param acknowledgment 手动确认
     */
    @KafkaListener(topics = "log-topic", groupId = "log-consumer-group",containerFactory = "kafkaListenerContainerFactory")
    public void consumeLog(@Payload String message, Acknowledgment acknowledgment) {
        try {
            // 解析日志JSON
            LogEntry logEntry = JSON.parseObject(message, LogEntry.class);
            
            // 保存到Elasticsearch
            logEntryRepository.save(logEntry);
            
            log.debug("日志已保存到ES - CaseId: {}, Message: {}", logEntry.getCaseId(), logEntry.getMessage());
            
            // 手动确认消费
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            
        } catch (Exception e) {
            log.error("消费日志失败 - Message: {}, Error: {}", message, e.getMessage(), e);
            // 不确认，让Kafka重新投递
        }
    }
}