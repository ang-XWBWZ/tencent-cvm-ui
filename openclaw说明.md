# tengxunCVM - OpenClaw 高效工作说明（面向 AI）

> 目标：让 AI 在本仓库“可持续运行”：少走弯路、少读无关文件、稳定交付。

## 1) 项目结构速览

- `src/`：后端主代码（Spring Boot）
- `frontend-elementplus/`：前端项目（Element Plus）
- `README.md` / `README_EN.md`：业务与使用说明
- `pom.xml`：后端 Maven 依赖与构建入口
- `main-change-log/`：变更沉淀目录（AI 每次有效改动后记录）

## 2) AI 首选阅读顺序（省 token）

1. `main说明.md`（本文件）
2. `main-change-log/INDEX.md`
3. 最近 1~3 条变更日志
4. `README.md`
5. 任务相关目录下的目标文件（按需精读）

## 3) 可持续运行规则

- 先定位问题再读代码，禁止无目标全仓扫描。
- 先做最小改动，再做最小验证。
- 发生有效变更必须写入 `main-change-log/`。
- 涉及凭据、密钥、账号信息时：默认不回显明文。

## 4) 发布/部署注意事项（CVM 项目）

> 以下为实操约定，避免每次口头重复；不记录任何密码明文。

- 工作目录：`/home/li/work/tengxunCVM`
- 发布分支：`main`
- 目标机：`<REDACTED_HOST>`（`ubuntu` 用户）
- 发布形态：前端编译产物进入 `src/main/resources/static`，再整体打包为 jar。
- 生产运行：`<REDACTED_DEPLOY_PATH>/tengxunCVM.jar`，端口 `<REDACTED_PORT>`。

### 发布前检查

1. 先确认正在运行的服务与端口：`java -jar ... --server.port=<REDACTED_PORT>`。
2. 变更已提交并推送到远端 `main` 分支。
3. 遵守安全约束：
   - 权限越大，责任越大
   - 严禁危险操作
   - 出现异常先上报，不做多次盲目重试

### 发布步骤（标准）

1. 拉取最新代码（`main`）
2. `frontend-elementplus` 执行 `npm ci && npm run build`
3. 根目录执行 `mvn -B clean package -DskipTests`
4. 替换目标 jar，重启进程
5. 本机回环检查：`curl http://127.0.0.1:<REDACTED_PORT>/`

### 验证点

- 进程在：`ps -ef | grep tengxunCVM.jar`
- 端口在：`ss -lntp | grep <REDACTED_PORT>`
- 页面可访问（至少根路径返回非错误连接）

### 回滚建议

- 发布前备份旧 jar（按时间戳）
- 新版本异常时，立即回滚旧 jar 并重启

## 5) 常用命令（最短路径）

### 仓库状态
```bash
git status --short --branch
git branch -a
```

### 后端构建
```bash
mvn -q -DskipTests package
```

### 前端开发（如需）
```bash
cd frontend-elementplus
npm install
npm run dev
```

## 5) 建议交付格式

1. 结论（1 句话）
2. 变更点（文件 + 动作）
3. 验证结果（命令 + 结果）
4. 风险与回滚（如有）

## 6) 项目现状总览（2026-02-24）

### 6.1 已实现（后端 API 维度）
- 凭证写入：`POST /api/credentials`
- 资源查询：`/api/regions`、`/api/zones`、`/api/instance-types`、`/api/images`
- 价格查询：`POST /api/price`
- 实例管理：`/api/instances`（列表/创建/开关机/重启/改名/销毁/续费）
- 账单相关接口：`/api/billing/*`（已接入真实 Tencent Billing OpenAPI，自封装 SDK）

### 6.2 已实现（前端页面维度）
- 采购页（多步骤配置 + 价格试算 + 模板导入导出）
- 实例管理页（列表、创建、开关机、重启、改名、销毁、续费）
- 账单管理页（余额、账单列表、详情弹窗）
- 中英双语切换、基础缓存（10 分钟）

### 6.3 当前主要短板（“部分功能 + 页面不够丰富”）
1. **账单能力已改为真实 OpenAPI，自封装 SDK 仍需继续沉淀**
   - 目前可用，但仍建议后续补充更完整的字段映射与账单明细维度。
2. **前端工程存在稳定性问题**
   - `InstanceManagement.vue` 使用了 `watch` 但未显式导入。
   - `BillingManagement.vue` 使用了 `onMounted` 但未显式导入。
3. **页面深度不足**
   - 缺少资源看板（地域分布、运行状态统计、成本趋势）。
   - 缺少高频运维动作聚合（批量操作、常用过滤收藏、快捷模板）。
4. **可观测性与可运维性不足**
   - 缺少统一日志分级、审计记录、失败重试与告警策略文档。

## 7) AI 可持续集成执行清单（建议）

### P0（优先）
1. 对齐 README 与真实实现差异（哪些是已完成、哪些是占位/mock）。
2. 修复前端编译级问题（缺失导入、基本联调自检）。
3. 将账单模块标注为“实验/占位”或补齐真实 SDK 实现。

### P1（体验增强）
1. 新增仪表盘页：实例总量、运行中/已停机、近 7/30 天费用趋势。
2. 实例列表支持组合筛选与批量操作。
3. 完善错误提示分层（用户可读提示 + 开发可定位错误码）。

### P2（工程化）
1. 补充前端单测（Vitest）与关键流程 E2E（Playwright/Cypress）。
2. 建立最小 CI：`mvn -q -DskipTests package` + `npm run build`。
3. 文档化发布流程（dev/staging/prod）与回滚手册。

### AI 执行约束（持续沿用）
- 每次任务必须包含：**范围声明 → 最小改动 → 最小验证 → 变更记录**。
- 每次有效改动后，在 `main-change-log/` 新增记录并更新 `INDEX.md`。
- 涉及凭据/密码/密钥：默认只做存在性与结构检查，不回显明文。


## 一键远端发布（推荐）

- 脚本：`<internal-deploy-script-removed>`
- 默认发布：`main` 分支到 `ubuntu@<REDACTED_HOST>:<REDACTED_PORT>`
- 用法：`bash <internal-deploy-script-removed>`
- 可选变量：`HOST` `BRANCH` `PORT` `APP_NAME`

说明：优先走脚本标准流程；仅在脚本失败时，再进入手动逐步排查。

## 镜像筛选与推荐（当前规则）

### 后端屏蔽关键词

以下关键词命中镜像名或系统名时，镜像会被直接过滤：

- `arm64`
- `aarch64`
- `tk4`
- `uefi`
- `tencentos`
- `grid`

> 匹配 **不区分大小写**。

### 后端推荐排序

镜像推荐排序由后端统一计算，前端不重复打分：

- `ubuntu`：+40
- `server`：+20
- `lts`：+20
- `20.04 / 22.04 / 24.04`：+10
- 公共镜像额外优先
- 创建时间较新的优先靠前

### 前端展示策略

- 提供“**仅看推荐镜像**”开关
- 默认开启
- 若推荐结果为空，则自动回退显示全部可用镜像

## 发布方式（固定流程）

### 推荐方式：脚本发布

已固化脚本：`<internal-deploy-script-removed>`

默认行为：

1. 目标机拉取 `main` 分支最新代码
2. 构建前端
3. 构建后端 jar
4. 替换生产 jar
5. 重启服务并做本机回环验活

### 使用方式

```bash
bash <internal-deploy-script-removed>
```

可选环境变量：

- `HOST`
- `BRANCH`
- `PORT`
- `APP_NAME`

### 约定

- **优先跑脚本**，不要每次手工敲一串命令
- 只有脚本失败时，才进入手动排查
- 发布后确认：
  - 服务端口：`<REDACTED_PORT>`
  - 运行 jar：`<REDACTED_DEPLOY_PATH>/tengxunCVM.jar`
  - 目标机：`ubuntu@<REDACTED_HOST>`

## 界面截图（2026-03-15）

当前购买页界面参考：

![TengxunCVM UI](docs/images/ui-screenshot-2026-03-15.png)

