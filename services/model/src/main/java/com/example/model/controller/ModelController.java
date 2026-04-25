package com.example.model.controller;

import com.example.model.dto.ModelTaskMessage;
import com.example.model.dto.CaseInfo;
import com.example.model.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Random;

/**
 * 模型图服务控制器
 * 提供模型任务管理和Kafka消息发送接口
 */
@RestController
@RequestMapping("/api/model")
@CrossOrigin(origins = "*")
public class ModelController {

    private static final Logger log = LoggerFactory.getLogger(ModelController.class);

    @Autowired
    private KafkaProducerService kafkaProducerService;

    /**
     * 发送模型任务消息到Kafka
     * 
     * @param message 模型任务消息
     * @return 发送结果
     */
    @PostMapping("/task/send")
    public ResponseEntity<Map<String, Object>> sendModelTask(@RequestBody ModelTaskMessage message) {
        log.info("接收到发送模型任务请求 - TaskId: {}, ModelId: {}", message.getTaskId(), message.getModelId());

        try {
            // 验证必填字段
            if (message.getTaskId() == null || message.getTaskId().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("任务ID不能为空"));
            }
            if (message.getModelId() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("模型ID不能为空"));
            }
            if (message.getSuiteId() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("测试集ID不能为空"));
            }
            if (message.getUserId() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户ID不能为空"));
            }

            // 发送消息到Kafka
            kafkaProducerService.sendModelTaskMessage(message);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "模型任务消息已发送到Kafka");
            response.put("taskId", message.getTaskId());
            response.put("modelId", message.getModelId());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("发送模型任务失败 - TaskId: {}, Error: {}", message.getTaskId(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("发送模型任务失败: " + e.getMessage()));
        }
    }

    /**
     * 同步发送模型任务消息到Kafka
     * 
     * @param message 模型任务消息
     * @return 发送结果
     */
    @PostMapping("/task/send-sync")
    public ResponseEntity<Map<String, Object>> sendModelTaskSync(@RequestBody ModelTaskMessage message) {
        log.info("接收到同步发送模型任务请求 - TaskId: {}, ModelId: {}", message.getTaskId(), message.getModelId());

        try {
            // 验证必填字段
            if (message.getTaskId() == null || message.getTaskId().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("任务ID不能为空"));
            }
            if (message.getModelId() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("模型ID不能为空"));
            }

            // 同步发送消息到Kafka
            boolean success = kafkaProducerService.sendModelTaskMessageSync(message);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "模型任务消息已同步发送到Kafka" : "模型任务消息发送失败");
            response.put("taskId", message.getTaskId());
            response.put("modelId", message.getModelId());
            response.put("timestamp", System.currentTimeMillis());

            return success ? ResponseEntity.ok(response) : ResponseEntity.internalServerError().body(response);

        } catch (Exception e) {
            log.error("同步发送模型任务失败 - TaskId: {}, Error: {}", message.getTaskId(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("同步发送模型任务失败: " + e.getMessage()));
        }
    }

    /**
     * 快速生成测试数据并发送
     * 
     * @param taskId 任务ID
     * @return 发送结果
     */
    @PostMapping("/task/send-test/{taskId}")
    public ResponseEntity<Map<String, Object>> sendTestMessage(@PathVariable String taskId) {
        log.info("接收到测试消息发送请求 - TaskId: {}", taskId);

        try {
            // 生成测试数据
            List<Integer> pathIds = new ArrayList<>();
            List<Integer> caseIds = new ArrayList<>();
            
            for (int i = 1; i <= 200; i++) {
                pathIds.add(i);
                caseIds.add(i);
            }

            ModelTaskMessage message = ModelTaskMessage.builder()
                .taskId(taskId)
                .modelId(123L)
                .suiteId(1234L)
                .userId(456L)
                .totalBatchCount(5)
                .batchIndex(0)
                .pathIds(pathIds)
                .caseIds(caseIds)
                .build();

            // 发送消息到Kafka
            kafkaProducerService.sendModelTaskMessage(message);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "测试模型任务消息已发送到Kafka");
            response.put("taskId", taskId);
            response.put("pathIdsCount", pathIds.size());
            response.put("caseIdsCount", caseIds.size());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("发送测试消息失败 - TaskId: {}, Error: {}", taskId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("发送测试消息失败: " + e.getMessage()));
        }
    }

    /**
     * 根据路径ID列表批量查询用例信息
     * 
     * @param pathIds 路径ID列表
     * @return 用例信息列表
     */
    @PostMapping("/cases/batch-query")
    public ResponseEntity<Map<String, Object>> batchQueryCases(@RequestBody List<Integer> pathIds) {
        log.info("接收到批量查询用例请求 - PathIds数量: {}", pathIds != null ? pathIds.size() : 0);

        try {
            if (pathIds == null || pathIds.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("路径ID列表不能为空"));
            }

            // 模拟根据pathIds查询用例信息
            // 实际应用中这里应该从数据库或其他数据源查询
            List<CaseInfo> caseInfos = generateMockCaseInfos(pathIds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", caseInfos);
            response.put("count", caseInfos.size());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("批量查询用例失败 - Error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("批量查询用例失败: " + e.getMessage()));
        }
    }

    /**
     * 生成模拟的用例信息
     * 实际应用中替换为真实的数据库查询
     */
    private List<CaseInfo> generateMockCaseInfos(List<Integer> pathIds) {
        Random random = new Random();
        return pathIds.stream()
            .map(pathId -> CaseInfo.builder()
                .pathId(String.valueOf(pathId))
                .caseId("CASE_" + pathId)
                .projectId(1L)
                .caseName("测试用例_" + pathId)
                .caseDesc("这是路径" + pathId + "的测试用例")
                .requestData("{\"url\":\"/api/test/" + pathId + "\",\"method\":\"POST\"}")
                .expectedResult("{\"code\":200,\"message\":\"success\"}")
                .caseType(random.nextInt(3) + 1) // 1-接口, 2-UI, 3-性能
                .priority(random.nextInt(3) + 1) // 1-高, 2-中, 3-低
                .build())
            .collect(Collectors.toList());
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}