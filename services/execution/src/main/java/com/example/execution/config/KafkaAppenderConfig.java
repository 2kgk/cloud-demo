package com.example.execution.config;

import com.example.execution.logging.KafkaAppender;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * KafkaAppender配置类
 */
@Configuration
public class KafkaAppenderConfig {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @PostConstruct
    public void init() {
        // 设置KafkaTemplate给KafkaAppender
        KafkaAppender.setKafkaTemplate(kafkaTemplate);
    }
}