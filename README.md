# CourtLink 运动场地预约系统

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

## 项目结构与分支管理

### 1. 项目分支结构
- `main`: 主分支，用于发布
- `develop`: 开发主分支
- `feat/user-management`: 用户管理功能分支
- `feat/court-management`: 场地管理功能分支
- `feat/booking-management`: 预约管理功能分支
- `feat/payment-integration`: 支付集成功能分支
- `feat/admin-management`: 后台管理功能分支

### 2. 前端项目结构
```
frontend/
├── src/
│   ├── assets/          # 静态资源
│   ├── components/      # 可复用组件
│   ├── views/           # 页面视图
│   ├── router/          # 路由配置
│   ├── store/          # 状态管理
│   ├── services/       # API服务
│   ├── utils/          # 工具函数
│   ├── styles/         # 全局样式
│   └── locales/        # 国际化
├── vite.config.js      # Vite配置
└── package.json        # 项目依赖
```

### 3. 后端项目结构
```
src/
├── main/
│   ├── java/
│   │   └── com/example/badminton/
│   │       ├── user/           # 用户模块
│   │       ├── court/          # 场地模块
│   │       ├── booking/        # 预约模块
│   │       ├── payment/        # 支付模块
│   │       ├── admin/          # 管理模块
│   │       └── common/         # 公共模块
│   └── resources/
│       └── application.properties
└── test/
    └── java/
```

## 开发流程

1. 分支管理流程
   - 从`main`分支创建`develop`分支作为开发主分支
   - 从`develop`分支创建功能分支进行开发
   - 功能开发完成后，合并回`develop`分支
   - 在`develop`分支进行集成测试
   - 测试通过后，合并到`main`分支进行发布

2. 功能分支职责

   ### feat/user-management
   - 用户注册、登录功能
   - 个人信息管理
   - 权限控制

   ### feat/court-management
   - 场地信息管理
   - 场地状态维护
   - 场地搜索功能

   ### feat/booking-management
   - 预约创建和取消
   - 预约时间冲突检查
   - 预约状态管理

   ### feat/payment-integration
   - 支付流程集成
   - 订单状态管理
   - 退款处理

   ### feat/admin-management
   - 系统配置管理
   - 用户管理
   - 数据统计

3. 开发优先级
   1. 用户管理模块（基础功能）
   2. 场地管理模块（核心功能）
   3. 预约管理模块（核心业务）
   4. 支付集成模块（流程完善）
   5. 后台管理模块（运营管理）

4. 开发规范
   - 代码提交前必须通过单元测试
   - 遵循Google Java代码规范
   - 使用Swagger注解维护API文档
   - 重要功能需要添加日志记录

5. 提交规范
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
## 开发规范
1. 新项目统一放在 `Projects` 目录下
2. 学习和练习代码放在 `Learning` 目录下
3. 实验性质的项目放在 `Others` 目录下
4. 每个项目必须包含自己的 README.md 文件
5. 遵循项目各自的代码规范和提交规范 