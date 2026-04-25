# ModelTaskConsumer 实现文档

## 功能概述

本功能实现了基于Kafka消息的消费处理，用于批量生成测试用例并管理批次进度。

## 核心功能

### 1. 幂等性检查
- 通过`ModelBatchProgressService.isBatchProcessed()`检查批次是否已处理
- 如果批次已处理，直接跳过消费，避免重复处理

### 2. 批量查询路径信息
- 使用`ModelCaseQueryService`批量查询用例信息
- 实现本地内存缓存，缓存容量10000条
- 缓存命中时直接返回，未命中时调用model服务查询

### 3. 并发生成Testcase
- 使用`ThreadPoolExecutor`线程池（核心4线程，最大8线程）
- 使用`CompletableFuture`异步生成每个测试用例
- 设置5分钟超时保护

### 4. 批次进度管理
- 初始化批次进度记录（第一次处理时）
- 每处理完一个批次，更新已处理批次数
- 所有批次完成后，标记任务为完成状态

## 技术架构

### 数据库表
- `model_batch_progress`: 批次进度表
  - 记录任务ID、模型ID、测试集ID
  - 记录总批次数、当前批次索引、已处理批次数
  - 状态：1-进行中，2-已完成

### 服务层
1. **ModelBatchProgressService**
   - 批次进度管理服务
   - 提供初始化、更新、查询、幂等性检查等方法

2. **ModelCaseQueryService**
   - Model用例查询服务
   - 包含本地缓存功能
   - 批量查询用例信息

3. **CaseService**
   - 测试用例服务
   - 批量保存测试用例到数据库

### 远程调用
- **ModelServiceClient**: Feign客户端，调用model服务获取用例信息
- **ModelController**: model服务接口，提供`/api/model/cases/batch-query`接口

## 消息格式

```json
{
  "taskId": "TASK_20250124_001",
  "modelId": 123,
  "suiteId": 456,
  "userId": 789,
  "totalBatchCount": 5,
  "batchIndex": 0,
  "pathIds": [1, 2, 3, 4, 5],
  "caseIds": [101, 102, 103, 104, 105]
}
```

## 处理流程

```
1. 接收Kafka消息
   ↓
2. 幂等性检查（是否已处理该批次？）
   ↓
3. 初始化/查询批次进度
   ↓
4. 批量查询用例信息（带缓存）
   ↓
5. 使用ThreadPoolExecutor+CompletableFuture并发生成testcase
   ↓
6. 批量保存testcase到数据库
   ↓
7. 更新批次进度
   ↓
8. 检查是否所有批次完成
   ↓
9. 如果完成，标记任务为完成状态
```

## 配置说明

### 线程池配置
```java
private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
    4,              // 核心线程数
    8,              // 最大线程数
    60L,            // 空闲线程存活时间（秒）
    TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(100),  // 任务队列容量
    new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
);
```

### 缓存配置
- 最大缓存容量：10000条
- 缓存满时清理10%的旧数据
- 使用`ConcurrentHashMap`保证线程安全

## 数据库表创建

执行SQL文件创建批次进度表：
```bash
mysql -u root -p your_database < model_batch_progress.sql
```

## 依赖配置

已在`services/case/pom.xml`中添加：
- Spring Cloud OpenFeign: 用于服务间调用
- Jackson Databind: 用于JSON序列化

## 启用Feign客户端

在启动类中添加`@EnableFeignClients`注解：
```java
@SpringBootApplication
@EnableFeignClients
public class CaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(CaseApplication.class, args);
    }
}
```

## 注意事项

1. **幂等性**: 通过批次进度表确保同一批次不会重复处理
2. **异常处理**: 单个用例生成失败不影响其他用例
3. **超时保护**: 设置5分钟超时，避免长时间阻塞
4. **缓存管理**: 自动清理旧缓存，防止内存溢出
5. **线程安全**: 使用并发集合和线程池保证并发安全

## 测试建议

1. 单元测试：测试各个Service的方法
2. 集成测试：测试完整的消息消费流程
3. 性能测试：测试大批次处理的性能
4. 异常测试：测试网络异常、数据库异常等情况

## 监控指标

建议监控以下指标：
- 消息消费速率
- 批次处理时长
- 缓存命中率
- 线程池使用情况
- 数据库操作耗时