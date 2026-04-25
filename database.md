```
CREATE TABLE `execution_result` (
`id` bigint(20) NOT NULL COMMENT '主键ID',
`task_id` bigint(20) NOT NULL COMMENT '执行任务ID',
`case_id` varchar(100) NOT NULL COMMENT '用例ID',
`case_table_suffix` varchar(10) DEFAULT NULL COMMENT '用例所在分表后缀(00-31)',
`execution_result` tinyint(4) NOT NULL COMMENT '执行结果(1-成功,2-失败)',
`execution_time` int(11) DEFAULT NULL COMMENT '执行耗时(毫秒)',
`error_message` text COMMENT '错误信息',
`actual_result` text COMMENT '实际结果',
`assert_result` tinyint(1) DEFAULT NULL COMMENT '断言结果(1-通过,0-不通过)',
`deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记(0-未删除,1-已删除)',
`created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
`updated_by` varchar(100) DEFAULT NULL COMMENT '更新人',
`created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (`id`),
KEY `idx_task_id` (`task_id`),
KEY `idx_case_id` (`case_id`),
KEY `idx_execution_result` (`execution_result`),
KEY `idx_created_time` (`created_time`),
KEY `idx_deleted` (`deleted`),
KEY `idx_case_table_suffix` (`case_table_suffix`),
KEY `idx_task_case` (`task_id`, `case_id`) COMMENT '任务-用例联合索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='执行结果表';
CREATE TABLE `project` (
`id` bigint(20) NOT NULL COMMENT '主键ID',
`project_name` varchar(255) NOT NULL COMMENT '项目名称',
`deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记(0-未删除,1-已删除)',
`created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
`updated_by` varchar(100) DEFAULT NULL COMMENT '更新人',
`created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (`id`),
KEY `idx_project_name` (`project_name`),
KEY `idx_deleted` (`deleted`),
KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';
CREATE TABLE `execution_task` (
`id` bigint(20) NOT NULL COMMENT '主键ID',
`task_name` varchar(255) NOT NULL COMMENT '任务名称',
`case_ids` text NOT NULL COMMENT '用例ID列表(JSON数组)',
`task_status` tinyint(4) NOT NULL COMMENT '任务状态(1-等待,2-执行中,3-执行结束)',
`total_cases` int(11) DEFAULT '0' COMMENT '总用例数',
`success_cases` int(11) DEFAULT '0' COMMENT '成功用例数',
`failed_cases` int(11) DEFAULT '0' COMMENT '失败用例数',
`start_time` datetime DEFAULT NULL COMMENT '开始时间',
`end_time` datetime DEFAULT NULL COMMENT '结束时间',
`deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记(0-未删除,1-已删除)',
`created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
`updated_by` varchar(100) DEFAULT NULL COMMENT '更新人',
`created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (`id`),
KEY `idx_task_status` (`task_status`),
KEY `idx_deleted` (`deleted`),
KEY `idx_created_time` (`created_time`),
KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='执行任务表';
CREATE TABLE `case_00` (
`id` bigint(20) NOT NULL COMMENT '主键ID',
`path_id` varchar(255) NOT NULL COMMENT '路径ID',
`case_id` varchar(100) NOT NULL COMMENT '用例ID',
`project_id` bigint(20) NOT NULL COMMENT '项目ID',
`case_name` varchar(500) DEFAULT NULL COMMENT '用例名称',
`case_desc` text COMMENT '用例描述',
`request_data` text COMMENT '请求数据',
`expected_result` text COMMENT '预期结果',
`case_type` tinyint(4) DEFAULT '1' COMMENT '用例类型(1-接口,2-UI,3-性能)',
`priority` tinyint(4) DEFAULT '3' COMMENT '优先级(1-高,2-中,3-低)',
`deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记(0-未删除,1-已删除)',
`created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
`updated_by` varchar(100) DEFAULT NULL COMMENT '更新人',
`created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (`id`),
UNIQUE KEY `uk_case_id` (`case_id`, `deleted`) COMMENT '用例ID唯一索引(考虑逻辑删除)',
KEY `idx_project_id` (`project_id`),
KEY `idx_path_id` (`path_id`),
KEY `idx_case_type` (`case_type`),
KEY `idx_priority` (`priority`),
KEY `idx_deleted` (`deleted`),
KEY `idx_created_time` (`created_time`),
KEY `idx_updated_time` (`updated_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用例表_分表00';
CREATE TABLE `id_segment` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
`biz_type` varchar(50) NOT NULL COMMENT '业务类型',
`max_id` bigint(20) NOT NULL COMMENT '当前最大ID',
`step` int(11) NOT NULL DEFAULT '1000' COMMENT '步长',
`version` int(11) NOT NULL DEFAULT '0' COMMENT '版本号(乐观锁)',
`description` varchar(255) DEFAULT NULL COMMENT '描述',
`created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (`id`),
UNIQUE KEY `uk_biz_type` (`biz_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主键号段表';

-- 初始化测试数据
INSERT INTO `id_segment` (`biz_type`, `max_id`, `step`, `description`) VALUES
('test_case', 1000, 1000, '测试用例主键'),
('project', 1000, 1000, '项目主键'),
('execution_task', 1000, 1000, '执行任务主键'),
('execution_result', 1000, 1000, '执行结果主键');
```
