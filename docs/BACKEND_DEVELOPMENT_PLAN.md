# 后端开发人员 - 开发计划

## ??? 开发人员信息
- **角色**: 后端开发（预约&支付模块）
- **负责分支**: `feat/booking-management`, `feat/payment-integration`
- **负责模块**: 预约管理模块(C) + 支付集成模块(D)

## ? 核心职责

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

## ? 详细开发任务

### 第一周：预约管理模块核心功能

#### Day 1-2: 预约实体和DTO设计
- [ ] 完善Appointment实体类
- [ ] 创建AppointmentDTO类
- [ ] 创建AppointmentRequest类
- [ ] 创建AppointmentResponse类
- [ ] 创建AppointmentQuery类
- [ ] 添加数据验证注解

#### Day 3-4: 预约服务实现
- [ ] 实现AppointmentServiceImpl类
  - [ ] createAppointment方法（包含时间冲突检查）
  - [ ] updateAppointment方法
  - [ ] cancelAppointment方法
  - [ ] confirmAppointment方法
  - [ ] completeAppointment方法
  - [ ] getAppointmentById方法
  - [ ] getAppointmentsByUserId方法
  - [ ] getAppointmentsByProviderId方法
  - [ ] getAppointmentsByStatus方法
  - [ ] hasTimeConflict方法
  - [ ] getAppointmentsByTimeRange方法
  - [ ] getUpcomingAppointments方法
  - [ ] cleanupExpiredAppointments方法
  - [ ] sendAppointmentReminder方法
  - [ ] sendAppointmentNotification方法

#### Day 5: 预约控制器实现
- [ ] 实现AppointmentController类
  - [ ] POST /api/appointments - 创建预约
  - [ ] PUT /api/appointments/{id} - 更新预约
  - [ ] DELETE /api/appointments/{id} - 取消预约
  - [ ] PUT /api/appointments/{id}/confirm - 确认预约
  - [ ] PUT /api/appointments/{id}/complete - 完成预约
  - [ ] GET /api/appointments/{id} - 获取预约详情
  - [ ] GET /api/appointments/user/{userId} - 获取用户预约列表
  - [ ] GET /api/appointments/provider/{providerId} - 获取服务提供者预约列表
  - [ ] GET /api/appointments/status/{status} - 根据状态获取预约列表
  - [ ] GET /api/appointments/conflict-check - 检查时间冲突
  - [ ] GET /api/appointments/statistics - 获取预约统计

### 第二周：支付集成模块核心功能

#### Day 1-2: 支付实体和DTO设计
- [ ] 完善Payment实体类
- [ ] 创建PaymentDTO类
- [ ] 创建PaymentRequest类
- [ ] 创建PaymentResponse类
- [ ] 创建PaymentCallback类
- [ ] 创建RefundRequest类
- [ ] 添加数据验证注解

#### Day 3-4: 支付服务实现
- [ ] 实现PaymentServiceImpl类
  - [ ] createPayment方法
  - [ ] processPayment方法
  - [ ] processMockPayment方法
  - [ ] handlePaymentCallback方法
  - [ ] processRefund方法
  - [ ] cancelPayment方法
  - [ ] getPaymentByPaymentNo方法
  - [ ] getPaymentsByUserId方法
  - [ ] getPaymentsByStatus方法
  - [ ] getPaymentsByPaymentMethod方法
  - [ ] getPaymentsByTimeRange方法
  - [ ] processTimeoutPayments方法
  - [ ] retryFailedPayment方法
  - [ ] sumAmountByUserIdAndStatus方法
  - [ ] calculateSuccessRate方法
  - [ ] generatePaymentNo方法
  - [ ] validatePayment方法
  - [ ] sendPaymentNotification方法

#### Day 5: 支付控制器实现
- [ ] 实现PaymentController类
  - [ ] POST /api/payments - 创建支付订单
  - [ ] POST /api/payments/{paymentNo}/process - 处理支付
  - [ ] POST /api/payments/{paymentNo}/mock - 模拟支付
  - [ ] POST /api/payments/callback - 支付回调
  - [ ] POST /api/payments/{paymentNo}/refund - 退款处理
  - [ ] DELETE /api/payments/{paymentNo} - 取消支付
  - [ ] GET /api/payments/{paymentNo} - 获取支付详情
  - [ ] GET /api/payments/user/{userId} - 获取用户支付列表
  - [ ] GET /api/payments/status/{status} - 根据状态获取支付列表
  - [ ] GET /api/payments/method/{method} - 根据支付方式获取支付列表
  - [ ] GET /api/payments/statistics - 获取支付统计

### 第三周：高级功能和集成

#### Day 1-2: 时间冲突检查算法
- [ ] 实现高效的时间冲突检查算法
- [ ] 添加时间缓冲区配置
- [ ] 实现冲突预约查询优化
- [ ] 添加冲突检查单元测试

#### Day 3-4: 支付状态机和流程
- [ ] 实现支付状态机
- [ ] 添加支付超时处理
- [ ] 实现支付重试机制
- [ ] 添加支付验证逻辑

#### Day 5: 通知系统集成
- [ ] 创建NotificationService接口
- [ ] 实现EmailNotificationService
- [ ] 实现SmsNotificationService
- [ ] 实现WebhookNotificationService
- [ ] 添加通知模板

### 第四周：定时任务和优化

#### Day 1-2: 定时任务实现
- [ ] 创建AppointmentScheduler类
- [ ] 实现清理过期预约任务
- [ ] 实现发送提醒任务
- [ ] 实现状态更新任务
- [ ] 添加定时任务配置

#### Day 3-4: 缓存和性能优化
- [ ] 实现Redis缓存配置
- [ ] 添加缓存注解
- [ ] 实现缓存更新策略
- [ ] 添加缓存单元测试

#### Day 5: 异常处理和日志
- [ ] 创建自定义异常类
- [ ] 创建全局异常处理器
- [ ] 添加异常日志记录
- [ ] 实现异常通知机制

## ?? 开发工具和命令

### 分支管理
```bash
# 切换到预约管理分支
git checkout feat/booking-management

# 切换到支付集成分支
git checkout feat/payment-integration

# 创建新的功能分支
git checkout -b feat/booking-conflict-check

# 推送分支到远程
git push -u origin feat/booking-management
```

### 开发流程
```bash
# 1. 拉取最新代码
git checkout develop
git pull origin develop

# 2. 创建功能分支
git checkout -b feat/feature-name

# 3. 开发功能...

# 4. 运行测试
mvn test

# 5. 代码格式化
mvn spotless:apply

# 6. 提交代码
git add .
git commit -m "feat(booking): 实现预约时间冲突检查功能"

# 7. 推送分支
git push origin feat/feature-name

# 8. 创建Pull Request
# 在GitHub上创建PR，从feat/feature-name到develop
```

### 测试命令
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=AppointmentServiceTest

# 运行特定测试方法
mvn test -Dtest=AppointmentServiceTest#shouldCheckTimeConflict

# 生成测试覆盖率报告
mvn jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html
```

### 代码质量检查
```bash
# 代码格式化
mvn spotless:apply

# 代码检查
mvn checkstyle:check

# 编译项目
mvn clean compile

# 打包项目
mvn clean package
```

## ? 代码提交规范

### 提交格式
```
<type>(<scope>): <subject>

type：feat | fix | docs | style | refactor | test | chore
scope：booking | payment | user | court | admin | ci | deps | *
subject：一句话中文描述，不超过 50 字
```

### 提交示例
```bash
# 预约管理模块
git commit -m "feat(booking): 实现预约创建功能"
git commit -m "feat(booking): 添加时间冲突检查算法"
git commit -m "fix(booking): 修复预约状态更新bug"
git commit -m "test(booking): 添加预约服务单元测试"

# 支付集成模块
git commit -m "feat(payment): 实现支付订单创建"
git commit -m "feat(payment): 添加支付状态机"
git commit -m "fix(payment): 修复支付回调处理"
git commit -m "test(payment): 添加支付服务测试"
```

## ? 单元测试要求

### 测试覆盖率要求
- **整体覆盖率**: ≥ 80%
- **Service层覆盖率**: ≥ 90%
- **Controller层覆盖率**: ≥ 70%
- **Repository层覆盖率**: ≥ 85%

### 测试示例
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

## ? 性能要求

### API响应时间
- **查询接口**: < 200ms
- **创建/更新接口**: < 500ms
- **复杂业务接口**: < 1000ms

### 并发要求
- **单接口并发**: > 100 QPS
- **系统整体并发**: > 1000 用户

### 数据库优化
- [ ] 添加必要的数据库索引
- [ ] 优化复杂查询
- [ ] 实现分页查询
- [ ] 添加查询缓存

## ? 代码审查要点

### 必须检查的项目
- [ ] 代码是否符合Google Java Style Guide
- [ ] 是否添加了必要的Swagger注解
- [ ] 是否添加了单元测试
- [ ] 是否添加了必要的日志记录
- [ ] 是否处理了异常情况
- [ ] 是否考虑了并发安全
- [ ] 是否添加了必要的注释

### 代码质量检查
```bash
# 运行所有检查
mvn clean verify

# 检查代码覆盖率
mvn jacoco:check

# 检查代码风格
mvn checkstyle:check
```

## ? 进度跟踪

### 每日任务
- [ ] 更新任务进度
- [ ] 提交代码到对应分支
- [ ] 运行单元测试
- [ ] 更新开发日志

### 每周回顾
- [ ] 检查代码质量指标
- [ ] 更新测试覆盖率
- [ ] 回顾开发进度
- [ ] 计划下周任务

## ? 部署和发布

### 开发环境
- 分支: `feat/booking-management`, `feat/payment-integration`
- 自动部署: 推送到分支时自动部署到开发环境

### 测试环境
- 分支: `develop`
- 自动部署: PR合并到develop时自动部署到测试环境

### 生产环境
- 分支: `main`
- 手动部署: 从main分支手动部署到生产环境

---

**注意**: 本计划会根据项目进展和需求变化进行调整，请定期查看最新版本。 