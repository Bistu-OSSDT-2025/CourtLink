# CourtLink - 羽毛球场预订管理系统

## 📋 项目概述

CourtLink 是一个基于 Spring Boot 3 的现代化羽毛球场预订系统，提供完整的预约管理、用户管理、场地管理和支付处理功能。

## 🚀 技术栈

### 后端技术
- **Framework**: Spring Boot 3.1.5
- **Language**: Java 21
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

## 📦 核心功能模块

### 1. 预约管理 (Booking Management)
- ✅ 创建预约
- ✅ 查看预约详情
- ✅ 取消预约
- ✅ 完成预约
- ✅ 按用户查询预约
- ✅ 按服务提供者查询预约
- ✅ 预约状态管理（PENDING, CONFIRMED, COMPLETED, CANCELLED）

### 2. 支付管理 (Payment Management)
- ✅ 支付处理（模拟）
- ✅ 退款处理（模拟）
- ✅ 支付状态跟踪
- ✅ 多种支付方式支持（ALIPAY, WECHAT_PAY, BANK_CARD）

### 3. 用户管理 (User Management)
- ✅ 用户注册 & 登录
- ✅ 用户信息 CRUD 操作
- ✅ 密码管理（修改、重置）
- ✅ 用户状态管理（激活、禁用）
- ✅ 用户名/邮箱唯一性检查

### 4. 场地管理 (Court Management)
- ✅ 场地信息 CRUD 操作
- ✅ 场地状态维护（可用、不可用、维护中）
- ✅ 场地搜索功能（按名称、位置、状态）
- ✅ 场地状态切换

### 5. 系统功能
- ✅ 健康检查端点
- ✅ 完整的 API 文档 (Swagger)
- ✅ 跨域资源共享 (CORS) 配置
- ✅ 统一异常处理
- ✅ 数据验证

## 🏗️ 项目结构

```
src/main/java/com/courtlink/
├── booking/                     # 预约管理模块
│   ├── entity/                 # 实体类
│   ├── dto/                    # 数据传输对象
│   ├── service/                # 业务逻辑层
│   ├── repository/             # 数据访问层
│   ├── controller/             # 控制器
│   └── BookingApplication.java # 启动类
├── payment/                    # 支付管理模块
│   ├── entity/                # 实体类
│   ├── dto/                   # 数据传输对象
│   ├── service/               # 业务逻辑层
│   ├── repository/            # 数据访问层
│   └── controller/            # 控制器
├── user/                      # 用户管理模块
│   ├── entity/                # 实体类
│   ├── dto/                   # 数据传输对象
│   ├── service/               # 业务逻辑层
│   ├── repository/            # 数据访问层
│   └── controller/            # 控制器
├── court/                     # 场地管理模块
│   ├── entity/                # 实体类
│   ├── dto/                   # 数据传输对象
│   ├── service/               # 业务逻辑层
│   ├── repository/            # 数据访问层
│   └── controller/            # 控制器
├── config/                    # 配置类
├── common/                    # 公共组件
│   ├── exception/            # 异常处理
│   ├── util/                 # 工具类
│   └── enums/                # 枚举类
└── CourtLinkApplication.java  # 应用主类
```

## 🛠️ 快速开始

### 环境要求
- Java 21+
- Maven 3.8+

### 运行步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd courtlink
```

2. **编译项目**
```bash
mvn clean compile
```

3. **运行测试**
```bash
mvn test
```

4. **启动应用**
```bash
mvn spring-boot:run
```

5. **访问应用**
- API 文档: http://localhost:8080/swagger-ui/index.html
- H2 数据库控制台: http://localhost:8080/h2-console
- 健康检查: http://localhost:8080/actuator/health

## 📚 API 文档

### 预约管理接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/appointments` | 创建预约 |
| GET | `/api/appointments/{id}` | 获取预约详情 |
| PUT | `/api/appointments/{id}` | 更新预约 |
| POST | `/api/appointments/{id}/cancel` | 取消预约 |
| POST | `/api/appointments/{id}/complete` | 完成预约 |
| GET | `/api/appointments/user/{userId}` | 获取用户预约列表 |
| GET | `/api/appointments/provider/{providerId}` | 获取提供者预约列表 |

### 支付管理接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/payments` | 创建支付 |
| GET | `/api/payments/{id}` | 获取支付详情 |
| POST | `/api/payments/{id}/refund` | 申请退款 |
| GET | `/api/payments/user/{userId}` | 获取用户支付记录 |

### 用户管理接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/users/register` | 用户注册 |
| POST | `/api/users/login` | 用户登录 |
| GET | `/api/users/{id}` | 获取用户信息 |
| PUT | `/api/users/{id}` | 更新用户信息 |
| DELETE | `/api/users/{id}` | 删除用户 |
| GET | `/api/users` | 获取用户列表（分页） |
| GET | `/api/users/check-username` | 检查用户名是否存在 |
| POST | `/api/users/{id}/change-password` | 修改密码 |

### 场地管理接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/courts` | 创建场地 |
| GET | `/api/courts/{id}` | 获取场地信息 |
| PUT | `/api/courts/{id}` | 更新场地信息 |
| DELETE | `/api/courts/{id}` | 删除场地 |
| GET | `/api/courts` | 搜索场地 |
| PATCH | `/api/courts/{id}/status` | 更改场地状态 |
