# 运动场地预约系统 (CourtLink)

## 项目简介

这是一个基于Spring Boot的运动场地预约系统，提供场地预约、用户管理、支付管理等功能。

## 技术栈

- Java 17
- Spring Boot 3.1.4
- Spring Security
- Spring Data JPA
- MySQL / H2 Database
- Thymeleaf
- JWT
- OpenAPI (Swagger)
- MapStruct
- Lombok
- JUnit 5
- Maven

## 模块划分与团队分工

### 1. 用户管理模块 (A)
- 核心功能：用户注册、登录、个人信息管理
- 技术要点：
  - Spring Security 用户认证
  - JWT token管理
  - 密码加密
  - 用户信息CRUD

### 2. 场地管理模块 (B)
- 核心功能：场地列表、详情、搜索
- 技术要点：
  - 场地信息管理
  - 场地状态维护
  - 搜索功能实现
  - 场地图片上传

### 3. 预约管理模块 (C)
- 核心功能：创建预约、取消预约、查看预约
- 技术要点：
  - 预约时间冲突检查
  - 预约状态管理
  - 预约通知
  - 定时任务处理

### 4. 支付集成模块 (D)
- 核心功能：模拟支付、订单状态更新
- 技术要点：
  - 支付流程设计
  - 订单状态管理
  - 支付回调处理
  - 退款处理

### 5. 后台管理模块 (E)
- 核心功能：场地审核、用户管理、数据统计
- 技术要点：
  - 权限管理
  - 审核流程
  - 数据统计报表
  - 系统配置管理

## 开发流程

1. 分支管理
   - main：主分支，用于发布
   - develop：开发主分支
   - feat/*：特性分支，如feat/user-management
   - fix/*：修复分支
   - release/*：发布分支

2. 开发规范
   - 代码提交前必须通过单元测试
   - 遵循Google Java代码规范
   - 使用Swagger注解维护API文档
   - 重要功能需要添加日志记录

3. 提交规范
   - feat: 新功能
   - fix: 修复bug
   - docs: 文档更新
   - style: 代码格式化
   - refactor: 代码重构
   - test: 测试相关
   - chore: 构建/工具相关

## 项目结构

```
src/main/java/com/example/badminton/
├── user/                    # 用户管理模块
├── court/                   # 场地管理模块
├── booking/                 # 预约管理模块
├── payment/                 # 支付集成模块
├── admin/                   # 后台管理模块
└── common/                  # 公共模块
```

## 开发环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- IDE (推荐使用IntelliJ IDEA或VS Code)

## 快速开始

1. 克隆项目
```bash
git clone https://github.com/your-username/badminton-court-booking.git
cd badminton-court-booking
```

2. 配置数据库
- 创建MySQL数据库
- 修改`application.properties`中的数据库配置

3. 构建和运行
```bash
mvn clean install
mvn spring-boot:run
```

4. 访问应用
- Web应用：http://localhost:8080
- API文档：http://localhost:8080/swagger-ui.html

## 接口文档

详细的API文档请参考：[API文档](http://localhost:8080/swagger-ui.html)

## 许可证

[MIT License](LICENSE)

# 工作空间项目组织

## 项目结构
```
BadmintonCourtBooking/
├── .mvn/                    # Maven包装器配置
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── badminton/
│   │   │               ├── admin/           # 管理员模块
│   │   │               │   ├── controller/  # 控制器
│   │   │               │   ├── service/     # 服务层
│   │   │               │   ├── repository/  # 数据访问层
│   │   │               │   └── model/       # 数据模型
│   │   │               │
│   │   │               ├── booking/         # 预约模块
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   └── model/
│   │   │               │
│   │   │               ├── court/           # 场地模块
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   └── model/
│   │   │               │
│   │   │               ├── payment/         # 支付模块
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   └── model/
│   │   │               │
│   │   │               ├── user/            # 用户模块
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   └── model/
│   │   │               │
│   │   │               └── common/          # 公共模块
│   │   │                   ├── config/      # 配置类
│   │   │                   ├── exception/   # 异常处理
│   │   │                   └── util/        # 工具类
│   │   │
│   │   └── resources/
│   │       ├── application.properties    # 主配置文件
│   │       ├── application-dev.properties # 开发环境配置
│   │       └── application-prod.properties # 生产环境配置
│   │
│   └── test/                  # 测试目录
│       └── java/
│           └── com/
│               └── example/
│                   └── badminton/
│                       ├── admin/
│                       ├── booking/
│                       ├── court/
│                       ├── payment/
│                       └── user/
│
├── .gitignore              # Git忽略文件
├── .gitattributes         # Git属性文件
├── CONTRIBUTING.md        # 贡献指南
├── mvnw                   # Maven包装器脚本（Unix）
├── mvnw.cmd              # Maven包装器脚本（Windows）
└── pom.xml               # Maven项目配置文件
```

## 项目说明

### 主要项目 (Projects)
- **BadmintonCourtBooking**: 羽毛球场地预约系统（新项目）
- **CourtLink**: 场地链接系统
- **frontend**: 前端项目

### 学习项目 (Learning)
- **Class_project**: 课程相关项目
- **Exam_project**: 考试相关项目
- **Practice**: 日常练习项目
- **Luogu_project**: 洛谷算法练习

### 其他项目 (Others)
- **Essentia**: Essentia相关项目
- **Help_project**: 帮助文档项目

## 开发规范
1. 新项目统一放在 `Projects` 目录下
2. 学习和练习代码放在 `Learning` 目录下
3. 实验性质的项目放在 `Others` 目录下
4. 每个项目必须包含自己的 README.md 文件
5. 遵循项目各自的代码规范和提交规范 