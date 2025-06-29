# 🏸 CourtLink 运动场地预约系统

一个基于 Spring Boot + Vue 的运动场地预约系统，支持用户注册登录、场地预约、支付集成与后台管理。

---

## 📌 项目简介

CourtLink 旨在为体育馆或场馆提供一个便捷高效的在线预约平台，用户可实时查看场地信息并进行预约支付，管理员可在线管理用户、场地与订单信息。

---

## 🚀 技术栈

### 🔧 后端
- Java 17
- Spring Boot 3.1.x
- Spring Security & JWT
- Spring Data JPA
- MySQL / H2
- MapStruct
- Swagger / OpenAPI
- Maven

### 🎨 前端
- Vue 3
- Vite
- Vue Router / Pinia
- Axios
- Element Plus
- i18n 国际化

---

## 🗂️ 项目结构

### 📁 后端结构
```
src/main/java/com/example/badminton/
├── user/           # 用户管理模块
├── court/          # 场地管理模块
├── booking/        # 预约管理模块
├── payment/        # 支付模块
├── admin/          # 后台管理模块
└── common/         # 公共类/配置/工具/异常
```

### 📁 前端结构
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

---

## 📄 功能模块

- ✅ 用户注册、登录、权限控制（JWT）
- ✅ 场地信息展示与管理
- ✅ 预约时间冲突判断与创建/取消预约
- ✅ 支付流程模拟（可扩展真实支付接口）
- ✅ 管理员后台管理界面（用户/订单/数据统计）

---

## 🧪 开发&测试环境要求

| 工具 | 版本要求 |
|------|-----------|
| Java | 17 或以上 |
| Maven | 3.8+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| IDE | IntelliJ IDEA / VS Code |

---

## ⚙️ 快速启动

### 后端运行

```bash
# 克隆项目
git clone https://github.com/Bistu-OSSDT-2025/CourtLink.git
cd CourtLink

# 配置数据库连接
vim src/main/resources/application.properties

# 编译 & 运行
mvn clean install
mvn spring-boot:run
```

访问：
* Web应用：`http://localhost:8080`
* Swagger API文档：`http://localhost:8080/swagger-ui.html`

### 前端运行

```bash
cd frontend
npm install
npm run dev
```

访问：
* 前端地址：`http://localhost:5173`

---

## 🔐 用户权限

| 角色 | 权限描述 |
|------|----------|
| 用户 | 预约场地、查看订单、个人信息修改 |
| 管理员 | 用户管理、场地管理、预约审核、数据查看 |

---

## 💡 项目规范

### 📁 分支管理

* `main`：主分支，用于发布
* `develop`：开发主分支
* `feat/user-management`：用户管理功能分支
* `feat/court-management`：场地管理功能分支
* `feat/booking-management`：预约管理功能分支
* `feat/payment-integration`：支付集成功能分支
* `feat/admin-management`：后台管理功能分支

### 🔤 提交规范

```bash
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式化
refactor: 代码重构
test: 测试相关
chore: 构建/工具相关
```

### 开发优先级
1. 用户管理模块（基础功能）
2. 场地管理模块（核心功能）
3. 预约管理模块（核心业务）
4. 支付集成模块（流程完善）
5. 后台管理模块（运营管理）

### 功能分支职责

#### feat/user-management
- 用户注册、登录功能
- 个人信息管理
- 权限控制

#### feat/court-management
- 场地信息管理
- 场地状态维护
- 场地搜索功能

#### feat/booking-management
- 预约创建和取消
- 预约时间冲突检查
- 预约状态管理

#### feat/payment-integration
- 支付流程集成
- 订单状态管理
- 退款处理

#### feat/admin-management
- 系统配置管理
- 用户管理
- 数据统计

---

## 📚 接口文档

使用 Swagger 自动生成，开发期间可通过浏览器访问：

🔗 `http://localhost:8080/swagger-ui.html`

---

## 🤝 贡献指南

欢迎任何形式的贡献！

* Fork 本仓库
* 创建分支：`git checkout -b feat/your-feature`
* 提交变更：`git commit -m 'feat: add awesome feature'`
* 推送分支：`git push origin feat/your-feature`
* 创建 Pull Request

详细说明参见：[CONTRIBUTING.md](CONTRIBUTING.md)

---

## 📝 许可证

本项目基于 [MIT License](LICENSE) 开源发布。

---

## 🙋‍♂️ 联系我们

如有问题或合作意向，可联系项目负责人：

📧 Email: [your-email@example.com](mailto:your-email@example.com)
📌 GitHub: [Bistu-OSSDT-2025](https://github.com/Bistu-OSSDT-2025)

--- 