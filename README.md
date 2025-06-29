# CourtLink 运动场地预约系统

## 项目简介
CourtLink是一个基于Spring Boot的运动场地预约管理系统，提供场地预约、用户管理、支付等功能。

## 技术栈
- Java 17
- Spring Boot 3.1.x
- Spring Data JPA
- H2 Database (开发环境)
- MySQL (生产环境)
- Maven
- Lombok
- Thymeleaf

## 功能模块
1. 用户模块
   - 用户注册
   - 用户登录
   - 个人信息管理

2. 场地管理模块
   - 场地信息维护
   - 场地状态管理
   - 场地查询

3. 预约模块
   - 预约创建
   - 预约取消
   - 预约查询

4. 支付模块
   - 在线支付
   - 订单管理
   - 退款处理

## 开发环境搭建
1. 安装JDK 17
2. 安装Maven
3. 克隆项目
4. 配置application.properties
5. 运行项目

## 项目结构
```
BadmintonCourtBooking/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           ├── user/       # 用户模块
│   │   │           ├── court/      # 场地模块
│   │   │           ├── booking/    # 预约模块
│   │   │           └── payment/    # 支付模块
│   │   └── resources/
│   │       ├── templates/          # Thymeleaf模板
│   │       ├── static/             # 静态资源
│   │       └── application.properties
│   └── test/
│       └── java/
├── pom.xml
└── README.md
```

## 开发规范
1. 代码规范遵循Google Java Style Guide
2. 提交规范遵循Angular提交规范
3. 分支管理遵循GitFlow工作流

## 团队分工
| 成员 | 模块                  | 关键任务                                |
|------|-----------------------|----------------------------------------|
| 组长 | 项目初始化            | 仓库/框架/CI配置                       |
| A    | 用户模块              | 注册/登录/个人信息管理                 |
| B    | 场地管理模块          | 场地CRUD/状态维护                      |
| C    | 预约模块              | 预约创建/取消/查询                     |
| D    | 支付模块              | 集成支付系统                           |
| E    | 前端页面              | 所有HTML模板/CSS                       | 