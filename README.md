# CourtLink - 羽毛球场预订管理系统

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 项目概述

CourtLink 是一个基于 Spring Boot 3 的现代化羽毛球场预订系统，采用模块化架构设计，提供完整的预约管理、用户管理、场地管理、管理员管理、支付处理和统计分析功能。

## 🚀 技术栈

### 后端技术
- **Framework**: Spring Boot 3.2.1
- **Language**: Java 21
- **Spring Data JPA** - 数据持久化和ORM
- **Spring Security** - 安全认证和授权
- **Spring Boot Validation** - 数据验证
- **Spring Boot Actuator** - 应用监控和健康检查

### 数据库
- **MySQL 8.0** - 生产环境数据库
- **H2 Database** - 测试环境内存数据库
- **HikariCP** - 高性能连接池

### 安全和认证
- **JWT (JSON Web Token)** - 无状态认证
- **Spring Security** - 安全框架
- **BCrypt** - 密码加密

### 缓存和性能
- **Caffeine Cache** - 高性能本地缓存
- **@Cacheable** - 声明式缓存

### 文档和API
- **SpringDoc OpenAPI 3** - API文档自动生成
- **Swagger UI** - 交互式API文档界面

### 开发工具
- **Lombok** - 减少样板代码
- **Maven** - 项目构建和依赖管理

## 📦 核心功能模块

### 1. 管理员模块 (Admin Management)
- ✅ 管理员认证和授权
- ✅ 系统管理功能
- ✅ 用户管理
- ✅ 场地管理
- ✅ 超级管理员和普通管理员分级

### 2. 预约管理 (Booking Management)
- ✅ 场地预订功能
- ✅ 预订状态管理
- ✅ 预订冲突检测
- ✅ 预订历史记录
- ✅ 智能调度算法

### 3. 场地模块 (Court Management)
- ✅ 场地信息管理
- ✅ 场地状态管理
- ✅ 场地调度功能
- ✅ 场地类型配置
- ✅ 批量操作支持

### 4. 用户管理 (User Management)
- ✅ 用户注册和登录
- ✅ 用户信息管理
- ✅ 用户权限控制
- ✅ 密码管理和重置

### 5. 支付管理 (Payment Management)
- ✅ 多种支付方式
- ✅ 支付状态跟踪
- ✅ 退款处理
- ✅ 支付安全保障

### 6. 统计模块 (Statistics)
- ✅ 场地使用统计
- ✅ 用户行为分析
- ✅ 收入统计
- ✅ 数据可视化

### 7. 系统功能
- ✅ JWT认证体系
- ✅ 完整的 API 文档 (Swagger)
- ✅ CORS 跨域配置
- ✅ 全局异常处理
- ✅ 数据验证和安全检查
- ✅ 健康检查和监控

## 🏗️ 项目结构

```
├── src/main/java/com/courtlink/          # 主要源码包
│   ├── admin/                            # 管理员模块
│   │   ├── controller/                   # 管理员控制器
│   │   ├── dto/                         # 数据传输对象
│   │   ├── entity/                      # 实体类
│   │   ├── repository/                  # 数据访问层
│   │   ├── service/                     # 业务逻辑层
│   │   ├── exception/                   # 异常处理
│   │   └── util/                        # 工具类
│   ├── booking/                         # 预订模块
│   ├── court/                           # 场地模块
│   ├── user/                            # 用户模块
│   ├── payment/                         # 支付模块
│   ├── statistics/                      # 统计模块
│   ├── security/                        # 安全模块
│   ├── config/                          # 全局配置
│   ├── common/                          # 公共模块
│   ├── util/                            # 全局工具类
│   └── CourtLinkApplication.java        # 主启动类
├── src/main/resources/
│   └── application.yml                  # 应用配置文件
├── src/test/java/                       # 测试代码
├── docs/                                # 项目文档
│   ├── reports/                         # 各种报告文档
│   └── tests/                           # 测试结果文件
├── scripts/                             # 脚本文件
│   ├── tests/                           # 测试脚本
│   └── utils/                           # 工具脚本
└── logs/                                # 日志目录
```

详细结构说明请参考 [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)

## 🛠️ 快速开始

### 环境要求
- Java 21+
- Maven 3.8+
- MySQL 8.0+ (生产环境)

### 运行步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd courtlink
```

2. **配置数据库**
```bash
# 创建MySQL数据库
mysql -u root -p
CREATE DATABASE courtlink;
```

3. **配置应用**
```yaml
# 编辑 src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/courtlink
    username: your_username
    password: your_password
```

4. **快速启动（开发环境）**
```bash
# 使用我们提供的启动脚本
.\scripts\utils\start-dev.ps1
```

或者手动启动：

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run
```

5. **访问应用**
- **API 基础路径**: http://localhost:8080/api
- **Swagger API 文档**: http://localhost:8080/api/swagger-ui.html
- **健康检查**: http://localhost:8080/api/actuator/health

## 🧪 测试

### 运行全量测试
```bash
# 使用测试脚本
.\scripts\tests\run-all-tests.ps1 -GenerateReport

# 或手动运行
mvn test
```

### 测试覆盖率
```bash
mvn jacoco:report
# 查看报告：target/site/jacoco/index.html
```

## 📚 API 文档

系统提供完整的 RESTful API，支持以下主要功能：

### 🔐 认证接口
- `POST /api/admin/login` - 管理员登录
- `POST /api/users/login` - 用户登录
- `POST /api/users/register` - 用户注册

### 👥 用户管理
- `GET /api/users` - 获取用户列表
- `GET /api/users/{id}` - 获取用户详情
- `PUT /api/users/{id}` - 更新用户信息

### 🏢 场地管理
- `GET /api/courts` - 获取场地列表
- `POST /api/courts` - 创建场地
- `PUT /api/courts/{id}` - 更新场地
- `GET /api/courts/{id}/schedule` - 获取场地调度

### 📅 预约管理
- `POST /api/appointments` - 创建预约
- `GET /api/appointments` - 查询预约
- `PUT /api/appointments/{id}` - 更新预约状态

### 💰 支付管理
- `POST /api/payments` - 创建支付
- `GET /api/payments/{id}` - 查询支付状态

### 📊 统计分析
- `GET /api/statistics/courts` - 场地统计
- `GET /api/statistics/revenue` - 收入统计

详细API文档请访问：http://localhost:8080/api/swagger-ui.html

## 🔧 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/courtlink
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
```

### JWT配置
```yaml
jwt:
  secret: your-secret-key
  expiration: 86400000 # 24 hours
```

### 管理员配置
```yaml
admin:
  super:
    username: superadmin
    password: SuperAdmin@2024
```

### 缓存配置
```yaml
courtlink:
  performance:
    enable-caching: true
    cache-expire-minutes: 5
```

## 🚀 部署

### 生产环境部署
```bash
# 构建生产包
mvn clean package -DskipTests

# 运行应用
java -jar target/courtlink-booking-1.0.0-SNAPSHOT.jar
```

### Docker 部署
```bash
# 构建镜像
docker build -t courtlink .

# 运行容器
docker run -p 8080:8080 courtlink
```

## 📖 文档

- [项目结构说明](PROJECT_STRUCTURE.md)
- [开发指南](docs/DEVELOPMENT_GUIDE.md)
- [API 文档](http://localhost:8080/api/swagger-ui.html)
- [部署指南](docs/DEPLOYMENT_GUIDE.md)

## 🤝 贡献

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目基于 MIT 许可证开源 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🎯 Roadmap

- [ ] 微信/支付宝支付集成
- [ ] 短信通知功能
- [ ] 移动端 APP 支持
- [ ] 多语言支持
- [ ] 数据导出功能
- [ ] 高级统计报表

## 💬 联系我们

如有问题或建议，请通过以下方式联系：

- 📧 Email: [your-email@example.com]
- 🐛 Issues: [GitHub Issues](https://github.com/your-repo/issues)
- 📖 Wiki: [项目Wiki](https://github.com/your-repo/wiki)
