# 项目开发规范指南

## 分支管理
- 主分支：main
- 功能分支命名规范：feat/模块名称
- 修复分支命名规范：fix/问题描述
- 发布分支命名规范：release/版本号

## 代码提交规范
提交信息格式：
```
<type>(<scope>): <subject>

<body>

<footer>
```

type类型：
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 重构
- test: 测试相关
- chore: 构建过程或辅助工具的变动

## 代码审查清单
- [ ] 代码是否符合项目规范
- [ ] 是否编写了单元测试
- [ ] 是否更新了API文档
- [ ] 是否处理了异常情况
- [ ] 是否有必要的注释
- [ ] 是否有重复代码
- [ ] 是否考虑了性能问题

## 开发流程
1. 从main分支创建功能分支
2. 在功能分支上进行开发
3. 编写单元测试
4. 更新API文档
5. 提交代码审查
6. 解决代码审查中的问题
7. 合并到main分支

## 模块职责
### 用户管理模块 (feat/user-management)
- 用户注册
- 用户登录
- 个人信息管理
- 权限控制

### 场地管理模块 (feat/court-management)
- 场地信息管理
- 场地状态管理
- 场地查询

### 预约管理模块 (feat/booking-management)
- 预约创建
- 预约查询
- 预约取消
- 预约变更

### 支付集成模块 (feat/payment-integration)
- 支付处理
- 退款处理
- 支付记录管理

### 后台管理模块 (feat/admin-management)
- 系统配置管理
- 用户管理
- 数据统计
- 日志管理

## API文档规范
使用Swagger注解对API进行文档化：
```java
@ApiOperation(value = "接口说明", notes = "详细描述")
@ApiParam(name = "参数名", value = "参数说明", required = true)
```

## 测试规范
- 单元测试覆盖率要求：>80%
- 测试命名规范：被测试类名Test
- 测试方法命名规范：test方法名_情景描述

## 异常处理
- 统一使用全局异常处理
- 自定义业务异常
- 异常信息国际化

## 日志规范
- 使用SLF4J+Logback
- 分级别记录日志
- 敏感信息脱敏 