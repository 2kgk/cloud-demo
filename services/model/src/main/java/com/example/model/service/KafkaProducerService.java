package com.example.model.service;

import com.alibaba.fastjson2.JSON;
import com.example.model.dto.ModelTaskMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka生产者服务
 * 用于发送模型任务消息到Kafka
 */
@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.model-task:test-topic}")
    private String modelTaskTopic;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 发送模型任务消息到Kafka
     *
     * @param message 模型任务消息
     */
    public void sendModelTaskMessage(ModelTaskMessage message) {
        try {
            String messageJson = JSON.toJSONString(message);
            log.info("准备发送消息到Kafka - Topic: {}, Message: {}", modelTaskTopic, messageJson);

            // 发送消息
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(modelTaskTopic, message.getTaskId(), messageJson);

            // 异步回调处理发送结果
            future.whenComplete((result, ex) ->
                    log.info("消息发送成功 - Topic: {}, Partition: {}, Offset: {}, TaskId: {}",
                            modelTaskTopic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            message.getTaskId())).handle((result, ex) -> {
                if (ex != null) {
                    log.error("消息发送失败 - Topic: {}, TaskId: {}, Error: {}",
                            modelTaskTopic, message.getTaskId(), ex.getMessage(), ex);
                }
                return null;
            });
        } catch (Exception e) {
            log.error("发送消息异常 - TaskId: {}, Error: {}", message.getTaskId(), e.getMessage(), e);
            throw new RuntimeException("发送Kafka消息失败", e);
        }
    }

    /**
     * 同步发送模型任务消息（等待发送结果）
     *
     * @param message 模型任务消息
     * @return 是否发送成功
     */
    public boolean sendModelTaskMessageSync(ModelTaskMessage message) {
        try {
            String messageJson = JSON.toJSONString(message);
            log.info("同步发送消息到Kafka - Topic: {}, Message: {}", modelTaskTopic, messageJson);

            kafkaTemplate.send(modelTaskTopic, message.getTaskId(), messageJson).get();

            log.info("同步消息发送成功 - TaskId: {}", message.getTaskId());
            return true;

        } catch (Exception e) {
            log.error("同步发送消息失败 - TaskId: {}, Error: {}", message.getTaskId(), e.getMessage(), e);
            return false;
        }
    }
}