# 预约管理与支付集成系统 - 开发指南

## 项目概述

本项目是一个基于Spring Boot的预约管理和支付集成系统，主要包含以下核心模块：

### 预约管理模块 (C)
- **核心功能**: 创建预约、取消预约、查看预约
- **技术要点**:
  - 预约时间冲突检查
  - 预约状态管理
  - 预约通知
  - 定时任务处理

### 支付集成模块 (D)
- **核心功能**: 模拟支付、订单状态更新
- **技术要点**:
  - 支付流程设计
  - 订单状态管理
  - 支付回调处理
  - 退款处理

## 技术栈

- **后端框架**: Spring Boot 2.7.x
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **消息队列**: RabbitMQ
- **API文档**: Swagger/OpenAPI 3
- **测试框架**: JUnit 5 + Mockito
- **构建工具**: Maven
- **代码规范**: Google Java Style Guide

## 开发环境搭建

### 1. 环境要求
- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+

### 2. 数据库初始化
```sql
-- 创建数据库
CREATE DATABASE appointment_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER 'appointment_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON appointment_system.* TO 'appointment_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 配置文件设置
复制 `src/main/resources/application.yml` 并根据本地环境修改配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/appointment_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
  
  rabbitmq:
    host: localhost
    port: 5672
    username: your_rabbitmq_username
    password: your_rabbitmq_password
```

## 分支管理策略

### 分支类型
- `main`: 主分支，用于生产发布
- `develop`: 开发主分支
- `feat/*`: 特性分支，如 `feat/appointment-management`
- `fix/*`: 修复分支
- `release/*`: 发布分支

### 工作流程

#### 1. 初始化项目
```bash
# 克隆项目
git clone <repository-url>
cd appointment-payment-system

# 初始化开发分支
./scripts/git-workflow.sh init
```

#### 2. 开发新功能
```bash
# 创建特性分支
./scripts/git-workflow.sh feature appointment-management

# 开发完成后提交代码
./scripts/git-workflow.sh commit feat "添加预约管理功能"

# 推送代码
./scripts/git-workflow.sh push

# 合并到develop分支
./scripts/git-workflow.sh merge-feature appointment-management
```

#### 3. 修复Bug
```bash
# 创建修复分支
./scripts/git-workflow.sh fix time-conflict-bug

# 修复完成后提交代码
./scripts/git-workflow.sh commit fix "修复时间冲突检查bug"

# 推送代码
./scripts/git-workflow.sh push

# 合并到develop分支
./scripts/git-workflow.sh merge-fix time-conflict-bug
```

#### 4. 发布版本
```bash
# 创建发布分支
./scripts/git-workflow.sh release 1.0.0

# 在发布分支上进行最终测试和修复

# 完成发布
./scripts/git-workflow.sh finish-release 1.0.0
```

## 代码规范

### 1. 提交规范
使用约定式提交规范：

- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式化
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建/工具相关

示例：
```bash
git commit -m "feat: 添加预约时间冲突检查功能"
git commit -m "fix: 修复支付回调处理中的空指针异常"
git commit -m "docs: 更新API文档"
```

### 2. 代码风格
遵循Google Java Style Guide：

```bash
# 运行代码检查
mvn checkstyle:check

# 格式化代码
mvn checkstyle:format
```

### 3. 命名规范
- **类名**: 使用PascalCase，如 `AppointmentService`
- **方法名**: 使用camelCase，如 `createAppointment`
- **变量名**: 使用camelCase，如 `appointmentId`
- **常量名**: 使用UPPER_SNAKE_CASE，如 `MAX_RETRY_COUNT`
- **包名**: 使用小写，如 `com.example.appointment`

## 开发流程

### 1. 功能开发流程

#### 步骤1: 需求分析
- 明确功能需求
- 设计API接口
- 定义数据模型

#### 步骤2: 创建分支
```bash
./scripts/git-workflow.sh feature feature-name
```

#### 步骤3: 开发实现
- 创建实体类 (Entity)
- 创建数据访问层 (Repository)
- 创建业务逻辑层 (Service)
- 创建控制器层 (Controller)
- 编写单元测试

#### 步骤4: 测试验证
```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify

# 运行代码检查
mvn checkstyle:check
```

#### 步骤5: 提交代码
```bash
./scripts/git-workflow.sh commit feat "功能描述"
./scripts/git-workflow.sh push
```

#### 步骤6: 合并分支
```bash
./scripts/git-workflow.sh merge-feature feature-name
```

### 2. 测试策略

#### 单元测试
- 使用JUnit 5编写测试
- 使用Mockito进行Mock
- 测试覆盖率要求 > 80%

示例：
```java
@Test
@DisplayName("创建预约时应该检查时间冲突")
void shouldCheckTimeConflictWhenCreatingAppointment() {
    // Given
    Appointment appointment = createTestAppointment();
    when(appointmentRepository.findConflictingAppointments(any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());
    
    // When
    Appointment result = appointmentService.createAppointment(appointment);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDING);
    verify(appointmentRepository).save(appointment);
}
```

#### 集成测试
- 测试数据库交互
- 测试API接口
- 测试外部服务集成

#### 端到端测试
- 测试完整业务流程
- 测试用户场景

### 3. 文档维护

#### API文档
使用Swagger注解维护API文档：

```java
@RestController
@RequestMapping("/api/appointments")
@Tag(name = "预约管理", description = "预约相关API")
public class AppointmentController {

    @PostMapping
    @Operation(summary = "创建预约", description = "创建新的预约记录")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "409", description = "时间冲突")
    })
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody Appointment appointment) {
        // 实现代码
    }
}
```

#### 代码注释
- 类和方法必须有JavaDoc注释
- 复杂逻辑需要行内注释
- 使用中文注释

## 部署指南

### 1. 开发环境
```bash
# 运行项目
mvn spring-boot:run

# 访问API文档
http://localhost:8080/api/swagger-ui.html
```

### 2. 测试环境
```bash
# 打包
mvn clean package -Dmaven.test.skip=true

# 运行
java -jar target/appointment-payment-system-1.0.0.jar --spring.profiles.active=test
```

### 3. 生产环境
```bash
# 打包
mvn clean package -Dmaven.test.skip=true -Pprod

# 运行
java -jar target/appointment-payment-system-1.0.0.jar --spring.profiles.active=prod
```

## 监控和日志

### 1. 日志配置
- 使用SLF4J + Logback
- 按级别分文件存储
- 定期归档和清理

### 2. 监控指标
- 应用性能监控 (APM)
- 数据库连接池监控
- 缓存命中率监控
- 支付成功率监控

### 3. 告警机制
- 异常告警
- 性能告警
- 业务告警

## 常见问题

### 1. 数据库连接问题
- 检查数据库服务是否启动
- 验证连接参数是否正确
- 确认数据库用户权限

### 2. Redis连接问题
- 检查Redis服务是否启动
- 验证连接参数是否正确
- 确认Redis密码设置

### 3. RabbitMQ连接问题
- 检查RabbitMQ服务是否启动
- 验证连接参数是否正确
- 确认队列和交换机配置

## 联系方式

如有问题，请联系开发团队或提交Issue。

---

**注意**: 本指南会随着项目发展持续更新，请定期查看最新版本。 