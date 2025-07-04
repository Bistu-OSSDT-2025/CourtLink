# 预定支付流程实现

## 功能概述

本次实现了完整的预定支付流程，包括：

1. **跳转成功即预定成功**：用户选择时间段后，跳转成功即设置预定状态为 `CONFIRMED`
2. **自动创建支付记录**：预定成功后自动创建支付订单
3. **10分钟支付超时机制**：未支付的订单将在10分钟后自动取消
4. **完整的支付页面**：显示预定信息、支付信息和倒计时

## 主要修改内容

### 1. 后端修改

#### 预定状态枚举 (`Appointment.java`)
```java
public enum AppointmentStatus {
    PENDING,    // 待支付
    CONFIRMED,  // 预定成功，待支付
    CANCELLED,  // 已取消
    COMPLETED   // 已完成
}
```

#### 支付实体 (`Payment.java`)
- 添加了 `appointmentId` 字段用于关联预定
- 支持多种支付状态：`PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`, `CANCELLED`

#### 定时任务服务 (`AppointmentScheduledService.java`)
- 每分钟检查一次过期的预定
- 自动取消10分钟内未支付的预定
- 释放相关的时间段资源
- 取消相关的支付订单

#### 预定控制器 (`AppointmentController.java`)
- 修改预定创建逻辑，直接设置状态为 `CONFIRMED`
- 自动创建支付记录
- 返回预定信息和支付信息

#### 数据库查询方法
- `AppointmentRepository.findByStatusAndCreatedAtBefore()` - 查询过期预定
- `PaymentRepository.findByAppointmentId()` - 根据预定ID查询支付记录

### 2. 前端修改

#### 支付页面 (`Payment.vue`)
- 显示预定信息（法院名称、日期、时间等）
- 显示支付信息（订单号、金额、支付方式）
- 10分钟倒计时功能
- 支付状态实时查询
- 支付处理和取消功能

#### 预定页面 (`Booking.vue`)
- 预定成功后直接跳转到支付页面
- 传递预定数据和支付数据

#### API服务 (`api.js`)
- 添加了完整的支付相关API方法
- 支持支付状态查询、支付处理、取消支付等

## 使用流程

### 1. 用户预定流程
1. 用户选择法院和时间段
2. 点击"确认预定"
3. 系统立即设置预定状态为 `CONFIRMED`
4. 自动创建支付记录
5. 跳转到支付页面

### 2. 支付页面功能
- 显示预定详情和支付信息
- 10分钟倒计时显示
- 支付按钮（集成微信支付等）
- 取消订单功能
- 支付状态实时更新

### 3. 自动取消机制
- 定时任务每分钟运行一次
- 检查10分钟前创建的 `CONFIRMED` 状态预定
- 如果未支付，自动取消预定和支付订单
- 释放时间段资源

## 技术特点

### 1. 状态管理
- 预定状态：`PENDING` → `CONFIRMED` → `COMPLETED`/`CANCELLED`
- 支付状态：`PENDING` → `PROCESSING` → `COMPLETED`/`FAILED`/`CANCELLED`

### 2. 定时任务
- 使用 `@Scheduled` 注解
- 每分钟检查过期订单
- 事务性操作确保数据一致性

### 3. 前端实时更新
- 支付页面倒计时
- 支付状态轮询
- 用户体验优化

### 4. 数据一致性
- 预定和支付记录关联
- 时间段资源管理
- 事务性操作

## 配置说明

### 1. 启用定时任务
在主应用类中添加：
```java
@SpringBootApplication
@EnableScheduling
public class CourtLinkApplication {
    // ...
}
```

### 2. 数据库支持
确保数据库表包含必要的字段：
- `appointment` 表需要 `created_at` 字段
- `payment` 表需要 `appointment_id` 字段

### 3. 前端路由配置
确保支付页面路由正确配置：
```javascript
{
  path: '/payment',
  name: 'Payment',
  component: () => import('@/views/Payment.vue')
}
```

## 测试

### 1. 运行测试脚本
```powershell
./test-appointment-flow.ps1
```

### 2. 手动测试步骤
1. 启动后端服务
2. 启动前端服务
3. 登录系统
4. 创建预定
5. 查看支付页面
6. 等待10分钟查看自动取消效果

### 3. 验证点
- 预定创建成功后状态为 `CONFIRMED`
- 支付记录自动创建
- 支付页面倒计时正常
- 10分钟后订单自动取消
- 时间段资源正确释放

## 注意事项

1. **时间同步**：确保服务器时间准确
2. **数据库性能**：定时任务查询可能需要索引优化
3. **前端状态**：处理网络中断等异常情况
4. **支付集成**：需要集成实际的支付网关
5. **日志记录**：重要操作都有日志记录

## 后续扩展

1. **支付方式**：支持多种支付方式（支付宝、银行卡等）
2. **通知功能**：支付成功/失败通知
3. **退款功能**：支持订单退款
4. **优惠券**：支持优惠券和折扣
5. **批量操作**：支持批量预定和支付

## 联系信息

如有问题或建议，请联系开发团队。 