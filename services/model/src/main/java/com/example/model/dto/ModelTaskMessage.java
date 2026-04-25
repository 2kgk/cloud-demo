package com.example.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 模型任务消息DTO
 * 用于发送到Kafka的消息结构
 */
@Data
public class ModelTaskMessage {

    /**
     * 生成任务名称
     */
    private String taskId;

    /**
     * 模型id
     */
    private Long modelId;

    /**
     * 测试集id
     */
    private Long suiteId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 总批次数
     */
    private Integer totalBatchCount;

    /**
     * 当前批次索引
     */
    private Integer batchIndex;

    /**
     * 路径id列表
     */
    private List<Integer> pathIds;

    /**
     * 用例id列表
     */
    private List<Integer> caseIds;

    /**
     * 构造器模式
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ModelTaskMessage message = new ModelTaskMessage();

        public Builder taskId(String taskId) {
            message.taskId = taskId;
            return this;
        }

        public Builder modelId(Long modelId) {
            message.modelId = modelId;
            return this;
        }

        public Builder suiteId(Long suiteId) {
            message.suiteId = suiteId;
            return this;
        }

        public Builder userId(Long userId) {
            message.userId = userId;
            return this;
        }

        public Builder totalBatchCount(Integer totalBatchCount) {
            message.totalBatchCount = totalBatchCount;
            return this;
        }

        public Builder batchIndex(Integer batchIndex) {
            message.batchIndex = batchIndex;
            return this;
        }

        public Builder pathIds(List<Integer> pathIds) {
            message.pathIds = pathIds;
            return this;
        }

        public Builder caseIds(List<Integer> caseIds) {
            message.caseIds = caseIds;
            return this;
        }

        public ModelTaskMessage build() {
            return message;
        }
    }
}