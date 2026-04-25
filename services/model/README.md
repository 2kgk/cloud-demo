# 模型图服务 - Kafka集成

## 项目概述

模型图服务是一个微服务，负责处理模型任务并通过Kafka消息队列将任务分发给case服务进行消费处理。

## 服务架构

```
┌─────────────────┐         Kafka         ┌─────────────────┐
│  Model Service  │  ─────────────────>  │   Case Service  │
│   (生产者)       │   test-topic        │   (消费者)       │
└─────────────────┘                      └─────────────────┘
```

## 服务端口

- **模型图服务**: 7084
- **Case服务**: 7082
- **Kafka**: localhost:9092

## 快速启动

### 1. 启动Kafka

确保Kafka服务已启动：

```bash
# 如果使用Docker
docker run -d -p 9092:9092 \
  -e KAFKA_ADVERTISED_HOST_NAME=localhost \
  -e KAFKA_ZOOKEEPER_CONNECT=172.17.0.1:2181 \
  wurstmeister/kafka:latest
```

### 2. 启动模型图服务

```bash
cd services/model
mvn clean install
mvn spring-boot:run
```

### 3. 启动Case服务

```bash
cd services/case
mvn clean install
mvn spring-boot:run
```

## API接口

### 1. 发送模型任务消息

**接口**: `POST /api/model/task/send`

**请求体**:
```json
{
  "taskId": "TASK_001",
  "modelId": 123,
  "suiteId": 1234,
  "userId": 456,
  "totalBatchCount": 5,
  "batchIndex": 0,
  "pathIds": [1, 2, 3, 4, 5],
  "caseIds": [1, 2, 3, 4, 5]
}
```

**响应**:
```json
{
  "success": true,
  "message": "模型任务消息已发送到Kafka",
  "taskId": "TASK_001",
  "modelId": 123,
  "timestamp": 1745467120000
}
```

### 2. 同步发送模型任务消息

**接口**: `POST /api/model/task/send-sync`

**请求体**: 同上

**响应**: 同上，但会等待Kafka确认

### 3. 快速测试接口

**接口**: `POST /api/model/task/send-test/{taskId}`

**示例**:
```bash
curl -X POST http://localhost:7084/api/model/task/send-test/TASK_TEST_001
```

**响应**:
```json
{
  "success": true,
  "message": "测试模型任务消息已发送到Kafka",
  "taskId": "TASK_TEST_001",
  "pathIdsCount": 200,
  "caseIdsCount": 200,
  "timestamp": 1745467120000
}
```

## 使用示例

### 1. 使用curl发送消息

```bash
curl -X POST http://localhost:7084/api/model/task/send \
  -H "Content-Type: application/json" \
  -d '{
    "taskId": "TASK_001",
    "modelId": 123,
    "suiteId": 1234,
    "userId": 456,
    "totalBatchCount": 5,
    "batchIndex": 0,
    "pathIds": [1,2,3,4,5],
    "caseIds": [1,2,3,4,5]
  }'
```

### 2. 使用Postman

1. 打开Postman
2. 创建POST请求：`http://localhost:7084/api/model/task/send`
3. 设置Headers：`Content-Type: application/json`
4. 在Body中粘贴上述JSON

### 3. 使用Java代码

```java
@RestController
public class TestController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @PostMapping("/test/send-model-task")
    public String testSendModelTask() {
        String url = "http://localhost:7084/api/model/task/send";
        
        Map<String, Object> message = new HashMap<>();
        message.put("taskId", "TASK_001");
        message.put("modelId", 123L);
        message.put("suiteId", 1234L);
        message.put("userId", 456L);
        message.put("totalBatchCount", 5);
        message.put("batchIndex", 0);
        message.put("pathIds", Arrays.asList(1, 2, 3, 4, 5));
        message.put("caseIds", Arrays.asList(1, 2, 3, 4, 5));
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, message, Map.class);
        return response.getBody().toString();
    }
}
```

## 消息格式

发送到Kafka的消息格式（JSON）：

```json
{
  "taskId": "TASK_001",
  "modelId": 123,
  "suiteId": 1234,
  "userId": 456,
  "totalBatchCount": 5,
  "batchIndex": 0,
  "pathIds": [1, 2, 3, 4, 5],
  "caseIds": [1, 2, 3, 4, 5]
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| taskId | String | 是 | 任务ID，唯一标识一个任务 |
| modelId | Long | 是 | 模型ID |
| suiteId | Long | 是 | 测试集ID |
| userId | Long | 是 | 用户ID |
| totalBatchCount | Integer | 是 | 总批次数 |
| batchIndex | Integer | 是 | 当前批次索引（从0开始） |
| pathIds | List<Integer> | 是 | 路径ID列表 |
| caseIds | List<Integer> | 是 | 用例ID列表 |

## 消费者处理逻辑

Case服务中的消费者会：

1. **监听Kafka主题** `test-topic`
2. **解析消息内容**为Map对象
3. **提取关键信息**（taskId, modelId, suiteId等）
4. **处理业务逻辑**（在`processModelTask`方法中实现）
5. **手动确认消息**消费成功

### 消费者日志示例

```
2026-04-24 12:30:00.123 [kafkaListenerContainer-0-C-1] INFO  ModelTaskConsumer - 收到模型任务消息 - Topic: test-topic, Partition: 0, Offset: 10, Key: TASK_001, Message: {"taskId":"TASK_001",...}
2026-04-24 12:30:00.124 [kafkaListenerContainer-0-C-1] INFO  ModelTaskConsumer - 任务详情 - TaskId: TASK_001, ModelId: 123, SuiteId: 1234, UserId: 456, Batch: 0/5, PathIds数量: 5, CaseIds数量: 5
2026-04-24 12:30:00.125 [kafkaListenerContainer-0-C-1] INFO  ModelTaskConsumer - 开始处理模型任务 - TaskId: TASK_001
2026-04-24 12:30:00.130 [kafkaListenerContainer-0-C-1] INFO  ModelTaskConsumer - 模型任务处理完成 - TaskId: TASK_001, 处理了 5 个用例, 5 个路径
2026-04-24 12:30:00.131 [kafkaListenerContainer-0-C-1] INFO  ModelTaskConsumer - 消息消费确认成功 - TaskId: TASK_001
```

## 配置说明

### Model服务配置

在 `services/model/src/main/resources/application.yml` 中：

```yaml
server:
  port: 7084

spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

kafka:
  topic:
    model-task: test-topic
```

### Case服务配置

在 `services/case/src/main/resources/application.yml` 中：

```yaml
kafka:
  topic:
    model-task: test-topic

spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: case-service-group
      enable-auto-commit: false
      auto-offset-reset: earliest
```

## 常见问题

### 1. Kafka连接失败

**问题**: 无法连接到Kafka服务器

**解决方案**: 
- 确保Kafka服务已启动
- 检查Kafka地址配置是否正确
- 验证网络连接

### 2. 消息未消费

**问题**: 消息发送成功但消费者未接收

**解决方案**:
- 检查消费者服务是否启动
- 确认topic名称是否一致
- 查看消费者日志

### 3. 消息重复消费

**问题**: 同一条消息被多次消费

**解决方案**:
- 检查消费者组ID配置
- 查看手动确认逻辑是否正确
- 考虑添加消息去重机制

## 扩展功能

### 1. 添加自定义业务逻辑

在 `ModelTaskConsumer.processModelTask()` 方法中添加您的业务逻辑：

```java
private void processModelTask(Map<String, Object> taskMessage) {
    String taskId = (String) taskMessage.get("taskId");
    
    // 1. 保存任务记录
    // TaskRecord record = new TaskRecord();
    // record.setTaskId(taskId);
    // taskMapper.insert(record);
    
    // 2. 处理测试用例
    List<Integer> caseIds = (List<Integer>) taskMessage.get("caseIds");
    // testCaseService.processTestCases(caseIds);
    
    // 3. 更新任务状态
    // taskMapper.updateStatus(taskId, "COMPLETED");
    
    log.info("模型任务处理完成 - TaskId: {}", taskId);
}
```

### 2. 添加消息持久化

在消费者中添加数据库持久化逻辑：

```java
@Autowired
private TaskMapper taskMapper;

private void processModelTask(Map<String, Object> taskMessage) {
    // 保存到数据库
    TaskRecord record = new TaskRecord();
    record.setTaskId((String) taskMessage.get("taskId"));
    record.setModelId((Long) taskMessage.get("modelId"));
    record.setStatus("PROCESSING");
    taskMapper.insert(record);
    
    // 处理逻辑...
    
    // 更新状态
    record.setStatus("COMPLETED");
    taskMapper.updateById(record);
}
```

### 3. 添加重试机制

```java
@Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
private void processModelTask(Map<String, Object> taskMessage) {
    // 处理逻辑
}
```

## 技术栈

- **Spring Boot 3.x**
- **Spring Kafka**
- **Kafka**
- **FastJSON2**
- **Lombok**
- **MyBatis-Plus** (Case服务)

## 联系方式

如有问题，请联系开发团队。