# 羽毛球场预约系统分支测试方案

## 1. 单元测试

### 1.1 实体类测试
- [ ] Booking 实体
  - 测试所有字段的 getter/setter
  - 测试时间计算逻辑
  - 测试实体关系映射
  - 测试 @PrePersist 和 @PreUpdate 逻辑
  - 测试版本控制字段

- [ ] Payment 实体
  - 测试支付状态转换
  - 测试金额计算
  - 测试与 Booking 的关联关系

- [ ] Court 实体
  - 测试场地状态变更
  - 测试场地类型验证
  - 测试价格范围验证

### 1.2 服务层测试
- [ ] BookingService
  ```java
  - createBooking()
    - 正常预约场景
    - 时间冲突场景
    - 无效时间段
    - 无效用户
    - 无效场地
  - updateBooking()
    - 状态更新
    - 时间修改
    - 并发修改处理
  - cancelBooking()
    - 正常取消
    - 已支付订单取消
    - 过期订单取消
  ```

- [ ] PaymentService
  ```java
  - processPayment()
    - 成功支付
    - 支付超时
    - 重复支付
    - 退款处理
  ```

- [ ] CourtService
  ```java
  - checkAvailability()
    - 空闲时段查询
    - 预约冲突检查
    - 维护时间段处理
  ```

### 1.3 控制器测试
- [ ] BookingController
  - 所有 API 端点的正常响应
  - 异常处理
  - 参数验证
  - 权限控制

- [ ] PaymentController
  - 支付流程测试
  - 回调处理
  - 异常处理

## 2. 集成测试

### 2.1 数据库集成测试
```java
- 实体关系映射测试
- 事务管理测试
- 并发操作测试
- 数据一致性测试
```

### 2.2 缓存集成测试
```java
- 缓存命中率测试
- 缓存失效测试
- 并发缓存访问测试
```

### 2.3 外部服务集成测试
```java
- 支付网关集成测试
- 短信服务集成测试
- 邮件服务集成测试
```

## 3. 性能测试

### 3.1 负载测试
```bash
- 并发预约测试（100用户）
- 高频查询测试（1000 QPS）
- 长时间运行测试（24小时）
```

### 3.2 压力测试
```bash
- 极限并发测试（500用户）
- 数据库压力测试
- 缓存压力测试
```

### 3.3 稳定性测试
```bash
- 7天持续运行测试
- 故障恢复测试
- 数据备份恢复测试
```

## 4. 安全测试

### 4.1 认证授权测试
```java
- JWT 令牌测试
- 角色权限测试
- 会话管理测试
```

### 4.2 数据安全测试
```java
- SQL 注入测试
- XSS 防护测试
- CSRF 防护测试
```

## 5. 边界测试

### 5.1 时间边界测试
```java
- 跨天预约测试
- 营业时间边界测试
- 节假日处理测试
```

### 5.2 数据边界测试
```java
- 最大预约数限制
- 金额计算精度
- 字段长度限制
```

## 6. 自动化测试脚本

### 6.1 单元测试脚本
```powershell
# 运行所有单元测试
./run-unit-tests.ps1

# 运行特定模块测试
./run-module-tests.ps1 -module booking
```

### 6.2 集成测试脚本
```powershell
# 运行所有集成测试
./run-integration-tests.ps1

# 运行特定场景测试
./run-scenario-tests.ps1 -scenario payment
```

### 6.3 性能测试脚本
```powershell
# 运行负载测试
./run-load-tests.ps1 -users 100 -duration 1h

# 运行压力测试
./run-stress-tests.ps1 -users 500 -duration 30m
```

## 7. 测试环境配置

### 7.1 测试数据库配置
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:courtlink-test
    username: sa
    password: password
```

### 7.2 测试缓存配置
```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=60s
```

### 7.3 测试日志配置
```yaml
logging:
  level:
    com.courtlink: DEBUG
    org.springframework: INFO
```

## 8. 测试报告模板

### 8.1 单元测试报告
```markdown
- 测试用例总数：
- 通过数量：
- 失败数量：
- 代码覆盖率：
- 问题清单：
```

### 8.2 性能测试报告
```markdown
- 并发用户数：
- 平均响应时间：
- 95% 响应时间：
- TPS：
- 错误率：
```

## 9. 回归测试清单

### 9.1 核心功能回归
- [ ] 预约流程
- [ ] 支付流程
- [ ] 取消流程
- [ ] 查询统计

### 9.2 非功能性回归
- [ ] 性能指标
- [ ] 安全特性
- [ ] 数据一致性
- [ ] 用户体验

## 10. 测试进度跟踪

### 10.1 每日测试报告
```markdown
日期：YYYY-MM-DD
- 已完成测试用例：
- 发现的问题：
- 解决的问题：
- 待解决问题：
```

### 10.2 问题跟踪
```markdown
- 问题编号：
- 严重程度：
- 影响范围：
- 解决状态：
- 解决方案：
``` 