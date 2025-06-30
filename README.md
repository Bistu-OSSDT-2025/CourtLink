# 预约管理与支付集成系统

## 项目概述

本项目是一个基于Spring Boot的预约管理和支付集成系统，主要包含以下核心模块：

- **预约管理模块 (C)**: 创建预约、取消预约、查看预约、时间冲突检查、状态管理、通知系统
- **支付集成模块 (D)**: 模拟支付、订单状态更新、支付回调、退款处理

## 技术栈

- **后端框架**: Spring Boot 2.7.x
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **消息队列**: RabbitMQ
- **API文档**: Swagger/OpenAPI 3
- **测试框架**: JUnit 5 + Mockito
- **构建工具**: Maven
- **代码规范**: Google Java Style Guide

## 项目结构

```
appointment-payment-system/
├── src/
│   ├── main/
│   │   ├── java/com/example/appointment/
│   │   │   ├── AppointmentApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   └── util/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/com/example/appointment/
├── docs/
├── scripts/
└── pom.xml
```

## 开发流程

### 分支管理
- `main`: 主分支，用于生产发布
- `develop`: 开发主分支
- `feat/*`: 特性分支，如 `feat/appointment-management`
- `fix/*`: 修复分支
- `release/*`: 发布分支

### 提交规范
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式化
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建/工具相关

## 快速开始

1. 克隆项目
```bash
git clone <repository-url>
cd appointment-payment-system
```

2. 创建并切换到开发分支
```bash
git checkout -b develop
```

3. 安装依赖
```bash
mvn clean install
```

4. 运行项目
```bash
mvn spring-boot:run
```

## API文档

启动项目后访问: http://localhost:8080/swagger-ui.html

## 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=AppointmentServiceTest
``` 