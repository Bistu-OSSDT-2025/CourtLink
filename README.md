# CourtLink Booking Management System

## 项目概述

CourtLink Booking Management System是一个现代化的预约和支付管理系统，用于管理球场预约、用户管理和支付处理。

## 技术栈

### 后端技术
- **Spring Boot 3.1.5** - 现代化的Java微服务框架
- **Java 21** - 最新的LTS Java版本
- **Spring Data JPA** - 数据持久化和ORM
- **Spring Boot Validation** - 数据验证
- **Spring Boot Actuator** - 应用监控和健康检查

### 数据库
- **H2 Database** - 内存数据库，适合开发和测试
- **Hibernate** - JPA实现

### 缓存和性能
- **Caffeine Cache** - 高性能本地缓存

### 文档和API
- **SpringDoc OpenAPI 3** - API文档自动生成
- **Swagger UI** - 交互式API文档界面

### 开发工具
- **Lombok** - 减少样板代码
- **Maven** - 项目构建和依赖管理

## 功能特性

### 预约管理
- ✅ 创建预约
- ✅ 查看预约详情
- ✅ 取消预约
- ✅ 完成预约
- ✅ 按用户查询预约
- ✅ 按服务提供者查询预约
- ✅ 预约状态管理（PENDING, CONFIRMED, COMPLETED, CANCELLED）

### 支付管理
- ✅ 支付处理（模拟）
- ✅ 退款处理（模拟）
- ✅ 支付状态跟踪
- ✅ 多种支付方式支持（ALIPAY, WECHAT_PAY, BANK_CARD）

### 用户管理
- ✅ 用户注册和登录
- ✅ 用户信息管理
- ✅ 密码加密
- ✅ 用户状态管理

### 球场管理
- ✅ 球场信息管理
- ✅ 球场状态管理
- ✅ 球场可用性检查

## 项目结构

```
src/main/java/com/courtlink/
├── booking/                     # 预约管理模块
│   ├── entity/                 # 实体类
│   ├── dto/                    # 数据传输对象
│   ├── service/                # 业务逻辑层
│   ├── repository/             # 数据访问层
│   ├── AppointmentController.java  # 预约控制器
│   ├── PaymentController.java     # 支付控制器
│   └── BookingApplication.java    # 启动类
├── config/                     # 配置类
├── controller/                 # 控制器
├── dto/                       # 数据传输对象
├── entity/                    # 实体类
├── enums/                     # 枚举类
├── repository/                # 数据访问层
├── service/                   # 业务逻辑层
├── user/                      # 用户管理模块
└── util/                      # 工具类
```

## 快速开始

### 环境要求
- Java 21+
- Maven 3.8+

### 运行应用

1. 克隆项目
```bash
git clone <repository-url>
cd courtlink-booking
```

2. 编译项目
```bash
mvn clean compile
```

3. 运行应用
```bash
mvn spring-boot:run
```

4. 访问应用
- 应用地址：http://localhost:8080
- Swagger UI：http://localhost:8080/swagger-ui/index.html
- API文档：http://localhost:8080/v3/api-docs

## API接口

### 预约管理API

#### 创建预约
```http
POST /api/appointments?userId={userId}
Content-Type: application/json

{
  "providerId": "provider123",
  "serviceType": "court_booking",
  "startTime": "2025-07-05T10:00:00",
  "endTime": "2025-07-05T11:00:00",
  "amount": 50.00,
  "notes": "Test booking"
}
```

#### 查看用户预约
```http
GET /api/appointments/user/{userId}?page=0&size=10
```

#### 查看预约详情
```http
GET /api/appointments/{id}
```

#### 取消预约
```http
POST /api/appointments/{id}/cancel
```

#### 完成预约
```http
POST /api/appointments/{id}/complete
```

### 支付管理API

#### 处理支付
```http
POST /api/payments/appointment/{appointmentId}/pay?paymentMethod=ALIPAY
```

#### 处理退款
```http
POST /api/payments/appointment/{appointmentId}/refund
```

### 用户管理API

#### 用户注册
```http
POST /api/users/register
```

#### 用户登录
```http
POST /api/users/login
```

### 球场管理API

#### 获取球场列表
```http
GET /api/courts?page=0&size=10
```

#### 创建球场
```http
POST /api/courts
```

## 配置

### 应用配置 (application.yml)

```yaml
server:
  port: 8080

spring:
  application:
    name: courtlink-booking
  
  # 数据库配置
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  # JPA配置
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
  # H2控制台
  h2:
    console:
      enabled: true
      path: /h2-console

# SpringDoc配置
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method

# 日志配置
logging:
  level:
    com.courtlink: DEBUG
    org.springframework.web: DEBUG
```

## 开发指南

### 代码规范
- 使用标准的Java命名约定
- 所有公共方法需要JavaDoc注释
- 使用@Valid注解进行参数验证
- 统一异常处理

### 数据库设计
- 使用JPA实体注解
- 遵循数据库命名规范
- 实体关系映射清晰

### API设计
- 遵循RESTful设计原则
- 统一返回格式
- 完整的API文档

## 部署

### 打包应用
```bash
mvn clean package
```

### 运行JAR包
```bash
java -jar target/courtlink-booking-1.0.0.jar
```

## 版本历史

### v1.0.0 (2025-07-02)
- ✅ Spring Boot 3.1.5技术栈升级
- ✅ Java 21支持
- ✅ 完整的预约管理功能
- ✅ 支付管理集成
- ✅ 用户管理模块
- ✅ 球场管理功能
- ✅ Swagger API文档
- ✅ 全面的异常处理
- ✅ 统一的响应格式

## 联系方式

- 项目维护：CourtLink Team
- 邮箱：support@courtlink.com

## 许可证

本项目采用 MIT 许可证。详情请参阅 [LICENSE](LICENSE) 文件。 