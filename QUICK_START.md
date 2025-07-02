# 预约管理与支付集成系统 - 快速开始指南

## ? 5分钟快速启动

### 1. 环境准备
确保您的系统已安装以下软件：
- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Git

### 2. 克隆项目
```bash
git clone <repository-url>
cd appointment-payment-system
```

### 3. 初始化Git分支
```bash
# 给脚本执行权限
chmod +x scripts/git-workflow.sh

# 初始化开发分支
./scripts/git-workflow.sh init
```

### 4. 配置数据库
```sql
-- 登录MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE appointment_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER 'appointment_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON appointment_system.* TO 'appointment_user'@'localhost';
FLUSH PRIVILEGES;
```

### 5. 修改配置文件
编辑 `src/main/resources/application.yml`，修改数据库连接信息：
```yaml
spring:
  datasource:
    username: your_username
    password: your_password
```

### 6. 启动项目
```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

### 7. 验证启动
访问以下地址验证项目是否启动成功：
- API文档: http://localhost:8080/api/swagger-ui.html
- 健康检查: http://localhost:8080/api/actuator/health

## ? 开发工作流程

### 开始新功能开发
```bash
# 1. 创建特性分支
./scripts/git-workflow.sh feature appointment-management

# 2. 开发功能...

# 3. 运行测试
./scripts/git-workflow.sh test

# 4. 提交代码
./scripts/git-workflow.sh commit feat "添加预约管理功能"

# 5. 推送代码
./scripts/git-workflow.sh push

# 6. 合并到develop分支
./scripts/git-workflow.sh merge-feature appointment-management
```

### 修复Bug
```bash
# 1. 创建修复分支
./scripts/git-workflow.sh fix time-conflict-bug

# 2. 修复问题...

# 3. 提交修复
./scripts/git-workflow.sh commit fix "修复时间冲突检查bug"

# 4. 推送并合并
./scripts/git-workflow.sh push
./scripts/git-workflow.sh merge-fix time-conflict-bug
```

## ? 核心功能开发

### 预约管理模块 (C)

#### 1. 创建预约
```java
// 创建预约请求
Appointment appointment = new Appointment();
appointment.setUserId("user123");
appointment.setProviderId("provider456");
appointment.setServiceType("咨询");
appointment.setStartTime(LocalDateTime.now().plusHours(1));
appointment.setEndTime(LocalDateTime.now().plusHours(2));
appointment.setAmount(new BigDecimal("100.00"));

// 保存预约
Appointment saved = appointmentService.createAppointment(appointment);
```

#### 2. 检查时间冲突
```java
// 检查是否存在时间冲突
boolean hasConflict = appointmentService.hasTimeConflict(
    "provider456", 
    startTime, 
    endTime, 
    null
);
```

#### 3. 取消预约
```java
// 取消预约
Appointment cancelled = appointmentService.cancelAppointment(
    appointmentId, 
    "用户主动取消"
);
```

### 支付集成模块 (D)

#### 1. 创建支付订单
```java
// 创建支付订单
Payment payment = new Payment();
payment.setPaymentNo("PAY" + System.currentTimeMillis());
payment.setAppointmentId("appointment123");
payment.setUserId("user123");
payment.setAmount(new BigDecimal("100.00"));
payment.setPaymentMethod(Payment.PaymentMethod.MOCK);

// 保存支付订单
Payment saved = paymentService.createPayment(payment);
```

#### 2. 处理支付
```java
// 处理模拟支付
Payment processed = paymentService.processMockPayment(paymentNo);
```

#### 3. 退款处理
```java
// 处理退款
Payment refunded = paymentService.processRefund(
    paymentNo, 
    new BigDecimal("100.00"), 
    "用户申请退款"
);
```

## ? 测试指南

### 运行单元测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=AppointmentServiceTest

# 运行特定测试方法
mvn test -Dtest=AppointmentServiceTest#shouldCreateAppointment
```

### 运行集成测试
```bash
# 运行集成测试
mvn verify
```

### 代码覆盖率检查
```bash
# 生成覆盖率报告
mvn jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html
```

## ? 监控和日志

### 查看应用日志
```bash
# 查看实时日志
tail -f logs/appointment-system.log

# 查看错误日志
grep ERROR logs/appointment-system.log
```

### 监控指标
- 应用性能: http://localhost:8080/api/actuator/metrics
- 健康状态: http://localhost:8080/api/actuator/health
- 应用信息: http://localhost:8080/api/actuator/info

## ? 常见问题

### Q: 数据库连接失败
**A:** 检查以下几点：
1. MySQL服务是否启动
2. 数据库连接参数是否正确
3. 数据库用户权限是否足够

### Q: Redis连接失败
**A:** 检查以下几点：
1. Redis服务是否启动
2. Redis连接参数是否正确
3. Redis密码设置是否正确

### Q: 项目启动失败
**A:** 检查以下几点：
1. JDK版本是否为11+
2. Maven是否正确安装
3. 配置文件是否有语法错误

### Q: 测试失败
**A:** 检查以下几点：
1. 测试数据库是否正确配置
2. 测试数据是否正确准备
3. 测试环境是否完整

## ? 更多资源

- [开发指南](docs/DEVELOPMENT_GUIDE.md) - 详细的开发文档
- [任务清单](docs/TASK_CHECKLIST.md) - 完整的开发任务列表
- [API文档](http://localhost:8080/api/swagger-ui.html) - 在线API文档

## ? 获取帮助

如果您在使用过程中遇到问题：

1. 查看 [常见问题](#常见问题) 部分
2. 查阅 [开发指南](docs/DEVELOPMENT_GUIDE.md)
3. 提交 Issue 到项目仓库
4. 联系开发团队

---

**祝您开发愉快！** ? 