# 云微服务演示项目

## 项目概述
这是一个基于Spring Cloud的微服务演示项目，包含产品服务和订单服务两个服务。

## 服务说明

### 服务注册与发现 (Service Discovery)

### 产品服务 (Product Service)
- 端口：7080
- 功能：提供产品查询接口
- API接口：
  - GET /api/products/{id} - 根据ID查询产品
  - GET /api/products/all - 获取所有产品

### 订单服务 (Order Service)
- 端口：7081
- 功能：提供订单查询接口，并集成产品服务获取产品详情
- API接口：
  - GET /api/orders/{id} - 根据ID查询订单（包含产品详情）
  - GET /api/orders/all - 获取所有订单（包含产品详情）

## 数据存储
所有服务均使用内存存储数据，不使用数据库。

## 启动顺序
1. 确保Nacos服务已启动 (默认地址: 127.0.0.1:8848)
2. 先启动产品服务
3. 再启动订单服务

## 测试接口

### 产品服务接口测试
```
# 查询单个产品
curl http://localhost:7080/api/products/1

# 查询所有产品
curl http://localhost:7080/api/products/all
```

### 订单服务接口测试
```
# 查询单个订单（包含产品详情）
curl http://localhost:7081/api/orders/1

# 查询所有订单（包含产品详情）
curl http://localhost:7081/api/orders/all
```

## 技术栈
- Spring Boot 3.3.4
- Spring Cloud 2023.0.3
- Spring Cloud Alibaba 2023.0.3.2
- Nacos (服务发现和配置中心)
- OpenFeign (服务间调用)