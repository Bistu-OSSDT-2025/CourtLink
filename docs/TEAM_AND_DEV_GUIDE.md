# 📋 分工 & 开发要求
> 项目：**CourtLink 运动场地预约系统**  
> 文档版本：v1.0 / 更新日期 2024-03-21

---

## 1. 团队成员与角色

| 成员 | GitHub ID | 角色 | 主要职责 | 负责分支 |
|------|-----------|------|----------|-----------|
| **组长** | @team-lead | 技术负责人 / DevOps | - 仓库 & CI/CD 配置<br>- 代码审查 / 合并<br>- 进度跟踪 | `main`, `develop` |
| **成员A** | @backend-user | 后端开发（用户模块） | - 用户注册/登录<br>- JWT+Spring Security<br>- 单元测试 | `feat/user-management` |
| **成员B** | @backend-court | 后端开发（场地模块） | - 场地 CRUD<br>- 状态维护 & 搜索 | `feat/court-management` |
| **成员C** | @backend-booking | 后端开发（预约&支付） | - 预约冲突检测<br>- 订单/支付逻辑 | `feat/booking-management`, `feat/payment-integration` |
| **成员D** | @frontend-dev | 前端开发 | - Vue3 架构 & 页面<br>- API 对接<br>- 响应式设计 | `feat/frontend` |

---

## 2. 分支策略

```
main        # 发布版本
develop     # 开发集成
feat/*      # 功能分支，来源 develop
fix/*       # 紧急修复，来源 main
hotfix/*    # 上线后紧急补丁，来源 main
release/*   # 发布准备，来源 develop
```

### 分支使用要点
1. **新功能** → 从 `develop` 派生 `feat/<模块名>` → 开发完成后 PR 回 `develop`  
2. **Bug fix** → 如线上问题紧急，直接在 `hotfix/*` 上修复并合回 `main` & `develop`  
3. **发版** → `release/*` 做版本号、文档、配置收尾 → 合 `main` → 打 Git tag → 再回合 `develop`

---

## 3. 开发流程

1. **任务认领**：在 GitHub Projects / Issue 中选择任务 → `Assignees` 设为自己  
2. **拉分支**：`git checkout -b feat/<task-name>`  
3. **本地开发**：代码 + 单元测试 + Swagger 注解  
4. **提交前自测**：`mvn test` / `npm run test` 必须通过  
5. **Push & PR**：标题示例 `feat(user): 完成注册接口`  
6. **Code Review**：≥ 1 人通过后可合并；组长在每日 17:00 前处理合并  
7. **CI 检查**：GitHub Actions 运行单测与 Lint；失败禁止合并  
8. **集成环境验证**：PR 合并到 `develop` 自动部署到 dev 服务器（Docker）

---

## 4. 代码提交规范（Commit Convention）

```
<type>(<scope>): <subject>

type：feat | fix | docs | style | refactor | test | chore
scope：user | court | booking | payment | admin | ci | deps | *
subject：一句话中文描述，不超过 50 字
```

**示例**
```
feat(booking): 新增预约冲突检测逻辑
fix(user): 修复 JWT 过期后无法刷新问题
docs: 更新快速开始章节
```

---

## 5. 代码质量要求

| 指标 | 后端 | 前端 |
|------|------|------|
| 单元测试覆盖率 | ≥ 80 % | ≥ 70 % |
| Lint 规则 | Google Java Style + Spotless | ESLint + Prettier |
| 必须日志点 | 入口、异常、支付回调 | Axios 拦截器、关键交互 |

---

## 6. CI/CD 约定

- **CI**：GitHub Actions  
  - `build-java.yml`：编译 + 单测 + Jacoco 覆盖率  
  - `build-frontend.yml`：`npm run lint && npm run build`
- **CD**（可选）：推 `main` 时触发 Docker Build & Deploy 到生产  
- **环境变量管理**：  
  - 本地 `.env.local`（不入库）  
  - CI 用 GitHub Secrets

---

## 7. 里程碑 & 进度

| 周数 | 目标 | 负责人 |
|------|------|--------|
| 第1周 | 项目骨架 & DB 设计 | 全员 |
| 第2周 | 用户 + 场地模块 API 完成 | A & B |
| 第3周 | 预约 + 支付完成；前端 UI 50 % | C & D |
| 第4周 | 管理后台初版；联调测试 | 全员 |
| 第5周 | Bug 修复、性能优化、上线准备 | 全员 |

---

## 8. 环境要求 & 常用命令

| 范围 | 工具 & 版本 | 启动命令 |
|------|-------------|----------|
| 后端 | Java 17, Maven 3.8 | `mvn spring-boot:run` |
| 前端 | Node 18+, pnpm 8 | `pnpm i && pnpm dev` |
| 代码格式化 | Spotless / Prettier | `mvn spotless:apply` / `pnpm lint --fix` |
| 数据库 | MySQL 8 | `docker compose up -d mysql` |

---

## 9. 风险与应对

| 风险点 | 应对措施 | 责任人 |
|--------|----------|--------|
| 需求变动频繁 | 每周评审需求；使用 Issue 跟踪 | 组长 |
| 进度延误 | Stand-up 及时暴露；必要时调配人力 | 全员 |
| 测试覆盖不足 | CI 阻断 & 增加测试任务 | 各模块负责人 |

---

## 10. 参考文档

- 《Google Java Style Guide》  
- 《Vue3 官方文档》  
- Swagger/OpenAPI 3.0 规范  
- Git Commit Message Standard

---

> **注意事项**  
> - 本文档将随项目进展持续更新，版本更新记录在顶部"文档版本"处
> - 团队成员请在阅读完本文档后在 Issue #1 下回复确认
> - 如有任何建议或疑问，请在团队周会上提出讨论 