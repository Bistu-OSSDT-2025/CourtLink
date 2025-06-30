# CourtLink API - 羽毛球场预订系统

## 📋 项目概述

CourtLink 是一个基于 Spring Boot 3 的现代化羽毛球场预订系统后端 API。项目采用最新的技术栈，提供完整的用户管理和场地管理功能。

## 🚀 技术栈

- **Framework**: Spring Boot 3.1.5
- **Language**: Java 21
- **Database**: H2 (开发环境)
- **Documentation**: OpenAPI 3 (Swagger)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Spring Boot Test

## 📦 核心功能模块

### 1. 用户管理模块 (User Management)
- ✅ 用户注册 & 登录
- ✅ 用户信息 CRUD 操作
- ✅ 密码管理（修改、重置）
- ✅ 用户状态管理（激活、禁用）
- ✅ 用户名/邮箱唯一性检查

### 2. 场地管理模块 (Court Management)
- ✅ 场地信息 CRUD 操作
- ✅ 场地状态维护（可用、不可用、维护中）
- ✅ 场地搜索功能（按名称、位置、状态）
- ✅ 场地状态切换

### 3. 系统功能
- ✅ 健康检查端点
- ✅ 完整的 API 文档 (Swagger)
- ✅ 跨域资源共享 (CORS) 配置
- ✅ 统一异常处理
- ✅ 数据验证

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
- 健康检查: http://localhost:8080/api/health/simple

## 📚 API 文档

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

### 健康检查接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/health` | 完整健康检查 |
| GET | `/api/health/simple` | 简单健康检查 |
| GET | `/api/health/ready` | 就绪检查 |
| GET | `/api/health/live` | 存活检查 |

## 🏗️ 项目结构

```
src/
├── main/
│   ├── java/
│   │   ├── com/bistu/ossdt/courtlink/user/    # 用户管理模块
│   │   │   ├── controller/                    # 控制器层
│   │   │   ├── service/                       # 服务层
│   │   │   ├── repository/                    # 数据访问层
│   │   │   ├── entity/                        # 实体类
│   │   │   ├── dto/                          # 数据传输对象
│   │   │   └── exception/                     # 异常处理
│   │   ├── com/courtlink/                     # 场地管理模块
│   │   │   ├── controller/                    # 控制器层
│   │   │   ├── service/                       # 服务层
│   │   │   ├── repository/                    # 数据访问层
│   │   │   ├── entity/                        # 实体类
│   │   │   ├── dto/                          # 数据传输对象
│   │   │   ├── config/                        # 配置类
│   │   │   └── enums/                         # 枚举类
│   │   └── CourtLinkApplication.java          # 应用主类
│   └── resources/
│       ├── application.properties             # 应用配置
│       └── application.yml                    # YAML配置
└── test/
    ├── java/
    │   ├── com/courtlink/service/             # 服务层测试
    │   ├── com/bistu/ossdt/courtlink/         # 应用测试
    │   └── ApplicationIntegrationTest.java    # 集成测试
    └── resources/
        └── application-test.properties        # 测试配置
```

## 📋 数据模型

### 用户实体 (User)
- `id`: 用户ID
- `username`: 用户名（唯一）
- `password`: 密码（加密存储）
- `email`: 邮箱（唯一）
- `phoneNumber`: 手机号
- `realName`: 真实姓名
- `avatar`: 头像URL
- `status`: 用户状态（ACTIVE/DISABLED/LOCKED）
- `role`: 用户角色（USER/ADMIN）

### 场地实体 (Court)
- `id`: 场地ID
- `name`: 场地名称
- `location`: 场地位置
- `description`: 场地描述
- `status`: 场地状态（AVAILABLE/UNAVAILABLE/MAINTENANCE）

## 🔧 配置说明

### 数据库配置
项目默认使用 H2 内存数据库，适合开发和测试。

```properties
# H2 数据库配置
spring.datasource.url=jdbc:h2:mem:courtlink
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 控制台
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### 跨域配置
已配置 CORS 支持前端开发，支持常见的前端开发服务器端口。

## 🧪 测试

### 运行所有测试
```bash
mvn test
```

### 运行特定测试
```bash
mvn test -Dtest=ApplicationIntegrationTest
```

### 测试覆盖率
项目包含：
- 单元测试：服务层逻辑测试
- 集成测试：端到端功能测试
- 应用测试：Spring Boot 应用启动测试

## 📈 开发规范

### 代码规范
- 遵循 Google Java Style Guide
- 使用 Lombok 减少样板代码
- 完整的 JavaDoc 注释
- 统一的异常处理

### API 设计规范
- RESTful API 设计
- 统一的响应格式
- 完整的 Swagger 注解
- 输入验证和错误处理

### 日志规范
- 使用 SLF4J + Logback
- 分级记录：DEBUG、INFO、WARN、ERROR
- 关键操作记录：创建、更新、删除、登录等

## 🚀 部署指南

### 打包应用
```bash
mvn clean package -DskipTests
```

### 运行 JAR 包
```bash
java -jar target/courtlink-api-0.0.1-SNAPSHOT.jar
```

### Docker 部署
```dockerfile
FROM openjdk:21-jre-slim
COPY target/courtlink-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'Add some amazing feature'`
4. 推送分支：`git push origin feature/amazing-feature`
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

- 项目维护者：CourtLink Team
- 邮箱：support@courtlink.com
- 文档：[API Documentation](http://localhost:8080/swagger-ui/index.html)

---

## ⚡ 快速验证

启动应用后，可以通过以下方式快速验证功能：

1. **健康检查**：访问 http://localhost:8080/api/health/simple
2. **API 文档**：访问 http://localhost:8080/swagger-ui/index.html
3. **数据库控制台**：访问 http://localhost:8080/h2-console
4. **用户注册测试**：使用 Swagger UI 测试用户注册接口
5. **场地管理测试**：使用 Swagger UI 测试场地创建接口

项目已完成基础功能开发，可以作为羽毛球场预订系统的后端 API 基础。
