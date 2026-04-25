-- 为model_batch_progress表添加processed_batch_indexes字段
-- 执行时间: 2026-04-24
-- 说明: 支持Kafka消息乱序消费，记录已处理的批次索引列表

ALTER TABLE `model_batch_progress` 
ADD COLUMN `processed_batch_indexes` text COMMENT '已处理的批次索引列表，逗号分隔，如"0,1,2,3"' 
AFTER `processed_batch_count`;

-- 为已有数据初始化processed_batch_indexes字段（可选）
-- 如果需要将现有数据迁移，可以执行以下更新语句：
-- UPDATE `model_batch_progress` 
-- SET `processed_batch_indexes` = CONCAT(REPEAT('0,', processed_batch_count), '0') 
-- WHERE `processed_batch_count` > 0 AND (`processed_batch_indexes` IS NULL OR `processed_batch_indexes` = '');
