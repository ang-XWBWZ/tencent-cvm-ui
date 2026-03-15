# 腾讯云 CVM 管理平台

一个基于 Spring Boot 和 Vue 3 的腾讯云云服务器（CVM）管理平台，提供实例查询、配置选择和价格计算等功能。

## 📋 项目简介

本项目是一个前后端分离的 Web 应用，用于管理和查询腾讯云 CVM 实例。通过友好的用户界面，您可以：

- 浏览和筛选可用的实例规格
- 查询不同配置的实时价格
- 查看和管理云服务器实例
- 导出/导入配置模板
- 支持多语言界面（中文/英文）

## ✨ 功能特性

### 核心功能

1. **凭证管理**
   - 动态配置腾讯云 SecretId/SecretKey
   - 支持设置默认地域
   - 凭证验证和状态提示

2. **资源查询**
   - 地域列表查询
   - 可用区列表查询
   - 实例规格列表（支持 CPU、内存、GPU 筛选）
   - 镜像列表查询
   - 实例列表查询（支持分页）

3. **价格计算**
   - 实时价格查询
   - 支持按量计费和竞价实例
   - 自动计算实例、带宽、磁盘价格
   - 智能可用区选择

4. **配置管理**
   - 多步骤配置向导
   - 配置模板导出/导入（JSON 格式）
   - 本地缓存机制（10分钟有效期）

5. **用户体验**
   - 响应式设计
   - 多语言支持（中文/英文）
   - 实时数据刷新
   - 智能筛选和搜索

## 🛠️ 技术栈

### 后端

- **框架**: Spring Boot 2.7.18
- **语言**: Java 8
- **构建工具**: Maven
- **SDK**: 腾讯云 SDK Java (tencentcloud-sdk-java-cvm 3.1.1290)

### 前端

- **框架**: Vue 3.4.38
- **UI 组件库**: Element Plus 2.8.4
- **构建工具**: Vite 5.4.2
- **状态管理**: Vue Composition API

## 📁 项目结构

```
tengxunCVM/
├── frontend-elementplus/          # 前端项目
│   ├── src/
│   │   ├── App.vue                # 主应用组件
│   │   ├── api.js                 # API 调用封装
│   │   ├── cache.js               # 缓存管理
│   │   ├── main.js                # 入口文件
│   │   └── style.css              # 样式文件
│   ├── index.html                 # HTML 模板
│   ├── package.json               # 前端依赖配置
│   └── vite.config.js             # Vite 配置
│
├── src/main/java/                 # 后端源码
│   └── com/example/tengxuncvm/
│       ├── service/               # 业务服务层
│       │   ├── CredentialStore.java          # 凭证管理
│       │   ├── CvmClientFactory.java         # CVM 客户端工厂
│       │   ├── RegionService.java            # 地域服务
│       │   ├── ZoneService.java              # 可用区服务
│       │   ├── InstanceTypeService.java      # 实例规格服务
│       │   ├── ImageService.java             # 镜像服务
│       │   ├── InstanceService.java          # 实例管理服务
│       │   ├── CvmPriceService.java          # 价格查询服务
│       │   ├── CvmInstanceCheckService.java  # 实例检查服务
│       │   ├── CvmInstanceDestroyService.java # 实例销毁服务
│       │   └── CvmInstanceRenewService.java  # 实例续费服务
│       └── web/                   # Web 控制器层
│           ├── CvmApiController.java         # REST API 控制器
│           ├── ApiExceptionHandler.java      # 异常处理
│           └── *.java                        # DTO 类
│
├── src/main/resources/
│   ├── application.properties     # 应用配置
│   └── static/                    # 静态资源（前端构建输出）
│
├── pom.xml                        # Maven 配置
└── README.md                      # 项目说明文档
```

## 🔧 环境要求

- **JDK**: 1.8 或更高版本
- **Maven**: 3.6 或更高版本
- **Node.js**: 16 或更高版本
- **npm**: 8 或更高版本

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd tengxunCVM
```

### 2. 配置后端

编辑 `src/main/resources/application.properties`：

```properties
# 腾讯云凭证配置（可选，也可通过前端界面配置）
tencentcloud.secret-id=your-secret-id
tencentcloud.secret-key=your-secret-key
tencentcloud.default-region=ap-guangzhou

# 服务器配置
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
```

### 3. 构建和运行后端

```bash
# 使用 Maven 构建
mvn clean package

# 运行 Spring Boot 应用
mvn spring-boot:run

# 或者运行 jar 包
java -jar target/tengxunCVM-0.0.1-SNAPSHOT.jar
```

后端服务默认运行在 `http://localhost:8080`

### 4. 构建和运行前端

```bash
# 进入前端目录
cd frontend-elementplus

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 构建生产版本（输出到 src/main/resources/static）
npm run build
```

前端开发服务器默认运行在 `http://localhost:5173`

### 5. 访问应用

- 开发模式：访问 `http://localhost:5173`
- 生产模式：访问 `http://localhost:8080`（前端已打包到后端静态资源）

## 📡 API 文档

### 基础路径

所有 API 的基础路径为：`/api`

### 接口列表

#### 1. 更新凭证

**POST** `/api/credentials`

更新腾讯云凭证信息。

**请求体：**
```json
{
  "secretId": "your-secret-id",
  "secretKey": "your-secret-key",
  "defaultRegion": "ap-guangzhou"
}
```

**响应：**
```json
{
  "status": "ok"
}
```

#### 2. 获取地域列表

**GET** `/api/regions`

获取所有可用的地域列表。

**响应：**
```json
["ap-guangzhou", "ap-shanghai", "ap-beijing", ...]
```

#### 3. 获取可用区列表

**GET** `/api/zones?region={region}`

获取指定地域的可用区列表。

**参数：**
- `region` (必需): 地域代码，如 `ap-guangzhou`

**响应：**
```json
["ap-guangzhou-1", "ap-guangzhou-2", "ap-guangzhou-3", ...]
```

#### 4. 获取实例规格列表

**GET** `/api/instance-types?region={region}`

获取指定地域的实例规格列表。

**参数：**
- `region` (必需): 地域代码

**响应：**
```json
[
  {
    "instanceType": "S1.SMALL1",
    "instanceFamily": "S1",
    "cpu": 1,
    "memory": 1,
    "gpu": 0,
    "zone": "ap-guangzhou-1"
  },
  ...
]
```

#### 5. 获取镜像列表

**GET** `/api/images?region={region}`

获取指定地域的镜像列表。

**参数：**
- `region` (必需): 地域代码

**响应：**
```json
[
  {
    "imageId": "img-xxx",
    "imageName": "Ubuntu Server 20.04",
    "imageOsName": "Ubuntu"
  },
  ...
]
```

#### 6. 查询价格

**POST** `/api/price`

查询指定配置的实例价格。

**请求体：**
```json
{
  "region": "ap-guangzhou",
  "zone": "ap-guangzhou-1",
  "instanceType": "S1.SMALL1",
  "imageId": "img-xxx",
  "systemDiskGb": 50,
  "dataDiskGb": 100,
  "bandwidthMbps": 5,
  "bandwidthChargeType": "BANDWIDTH_POSTPAID_BY_HOUR",
  "instanceChargeType": "POSTPAID_BY_HOUR",
  "scheduledDestroy": false
}
```

**响应：**
```json
{
  "instancePrice": 0.12,
  "bandwidthPrice": 0.25,
  "systemDiskPrice": 0.0,
  "dataDiskPrice": 0.0,
  "totalPrice": 0.37,
  "currency": null,
  "zone": "ap-guangzhou-1",
  "scheduledDestroy": false
}
```

#### 7. 获取实例列表

**GET** `/api/instances?region={region}&page={page}&size={size}`

获取指定地域的实例列表（分页）。

**参数：**
- `region` (必需): 地域代码
- `page` (可选): 页码，默认 1
- `size` (可选): 每页数量，默认 10，最大 50

**响应：**
```json
{
  "totalCount": 10,
  "page": 1,
  "size": 10,
  "items": [
    {
      "instanceId": "ins-xxx",
      "instanceName": "test-instance",
      "instanceType": "S1.SMALL1",
      "cpu": 1,
      "memory": 1,
      "zone": "ap-guangzhou-1",
      "instanceChargeType": "POSTPAID_BY_HOUR",
      "instanceState": "RUNNING"
    },
    ...
  ]
}
```

## 💡 使用说明

### 首次使用

1. **配置凭证**
   - 点击右上角"凭证"按钮
   - 输入腾讯云 SecretId 和 SecretKey
   - 设置默认地域（可选）
   - 点击"保存"

2. **选择地域和计费方式**
   - 在步骤 1 中选择地域
   - 选择实例计费方式（按量计费/竞价）

3. **选择实例规格**
   - 在步骤 2 中浏览实例规格列表
   - 使用筛选条件（关键词、规格族、CPU、内存、GPU）筛选
   - 点击表格行选择实例规格

4. **配置存储和网络**
   - 在步骤 3 中选择镜像
   - 配置系统盘和数据盘大小
   - 配置带宽和计费方式

5. **查看价格和导出配置**
   - 在步骤 4 中点击"计算价格"查看总价
   - 可以导出配置模板或导入已有模板

### 高级功能

- **配置模板**：导出当前配置为 JSON 文件，方便下次导入使用
- **缓存机制**：前端自动缓存 API 响应，10 分钟内有效
- **多语言**：支持中文和英文切换
- **实时刷新**：点击"刷新列表"或"刷新价格"更新数据

## ⚠️ 注意事项

1. **凭证安全**
   - 凭证信息存储在浏览器本地存储中
   - 生产环境建议使用更安全的凭证管理方式
   - 不要将包含凭证的配置文件提交到版本控制系统

2. **API 限制**
   - 腾讯云 API 有调用频率限制
   - 前端已实现缓存机制减少 API 调用
   - 建议合理使用刷新功能

3. **价格查询**
   - 价格查询会自动尝试多个可用区
   - 如果指定可用区不支持该实例规格，会自动选择其他可用区
   - 价格信息仅供参考，实际价格以腾讯云控制台为准

4. **浏览器兼容性**
   - 建议使用现代浏览器（Chrome、Firefox、Edge 等）
   - 需要支持 ES6+ 和 LocalStorage

## 🐳 Docker 部署

### 1. 使用 Docker Compose（推荐）

创建 `docker-compose.yml` 文件：

```yaml
version: '3.8'

services:
  backend:
    build:
      context: .
      dockerfile: Dockerfile.backend
    ports:
      - "8080:8080"
    environment:
      - TENCENTCLOUD_SECRET_ID=${TENCENTCLOUD_SECRET_ID}
      - TENCENTCLOUD_SECRET_KEY=${TENCENTCLOUD_SECRET_KEY}
      - TENCENTCLOUD_DEFAULT_REGION=${TENCENTCLOUD_DEFAULT_REGION:-ap-guangzhou}
    volumes:
      - ./logs:/app/logs
    restart: unless-stopped

  frontend:
    build:
      context: ./frontend-elementplus
      dockerfile: Dockerfile.frontend
    ports:
      - "5173:5173"
    depends_on:
      - backend
    restart: unless-stopped
```

### 2. 创建 Dockerfile

**后端 Dockerfile (`Dockerfile.backend`):**
```dockerfile
FROM maven:3.8.4-openjdk-8-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**前端 Dockerfile (`frontend-elementplus/Dockerfile.frontend`):**
```dockerfile
FROM node:16-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 3. 环境变量配置

创建 `.env` 文件：

```env
TENCENTCLOUD_SECRET_ID=your-secret-id
TENCENTCLOUD_SECRET_KEY=your-secret-key
TENCENTCLOUD_DEFAULT_REGION=ap-guangzhou
```

### 4. 启动服务

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 5. 访问应用

- 后端 API: http://localhost:8080
- 前端界面: http://localhost:5173
- 生产模式（Nginx）: http://localhost:80

## 🔧 环境变量配置

除了在 `application.properties` 中配置，还支持通过环境变量配置：

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| `TENCENTCLOUD_SECRET_ID` | 腾讯云 SecretId | 无 |
| `TENCENTCLOUD_SECRET_KEY` | 腾讯云 SecretKey | 无 |
| `TENCENTCLOUD_DEFAULT_REGION` | 默认地域 | `ap-guangzhou` |
| `SERVER_PORT` | 服务器端口 | `8080` |
| `SPRING_PROFILES_ACTIVE` | Spring 环境 | `dev` |

### 配置优先级

1. 环境变量（最高优先级）
2. `application.properties` 文件
3. 前端界面配置

## 🐛 故障排除

### 常见问题

#### 1. API 调用失败
- **症状**: 前端显示"网络错误"或"API调用失败"
- **可能原因**: 
  - 凭证无效或过期
  - 网络连接问题
  - 腾讯云 API 服务异常
- **解决方案**:
  1. 检查凭证是否正确
  2. 验证网络连接
  3. 查看浏览器控制台错误信息
  4. 检查后端日志

#### 2. 价格查询返回空值
- **症状**: 价格计算显示为0或空
- **可能原因**:
  - 实例规格在所选地域不可用
  - 腾讯云价格API返回异常
- **解决方案**:
  1. 尝试其他地域
  2. 检查实例规格是否支持
  3. 查看后端日志中的详细错误

#### 3. 前端缓存问题
- **症状**: 数据不更新或显示旧数据
- **解决方案**:
  1. 清除浏览器缓存
  2. 点击"刷新列表"按钮
  3. 等待缓存过期（10分钟）

#### 4. Docker 容器启动失败
- **症状**: `docker-compose up` 失败
- **解决方案**:
  1. 检查 Docker 和 Docker Compose 版本
  2. 确保 `.env` 文件存在且配置正确
  3. 查看容器日志: `docker-compose logs [service-name]`

### 日志查看

#### 后端日志
```bash
# 查看 Spring Boot 应用日志
docker-compose logs backend

# 实时查看日志
docker-compose logs -f backend

# 进入容器查看
docker-compose exec backend tail -f /app/logs/application.log
```

#### 前端日志
- 浏览器开发者工具（F12）→ Console 标签页
- 浏览器开发者工具 → Network 标签页查看API请求

## 🧪 测试说明

### 后端测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=TengxunCvmApplicationTests

# 生成测试覆盖率报告
mvn clean test jacoco:report
```

### 测试结构

```
src/test/java/com/example/tengxuncvm/
└── TengxunCvmApplicationTests.java    # 应用上下文测试
```

### 前端测试

当前项目未配置前端测试框架，建议添加：

1. **单元测试**: 使用 [Vitest](https://vitest.dev/)
2. **组件测试**: 使用 [Vue Test Utils](https://test-utils.vuejs.org/)
3. **E2E 测试**: 使用 [Cypress](https://www.cypress.io/) 或 [Playwright](https://playwright.dev/)

### 添加测试示例

**安装 Vitest:**
```bash
cd frontend-elementplus
npm install -D vitest @vue/test-utils jsdom
```

**创建测试文件 `src/__tests__/api.test.js`:**
```javascript
import { describe, it, expect } from 'vitest'
import { calculateTotalPrice } from '../api.js'

describe('API 函数测试', () => {
  it('应该正确计算总价', () => {
    const instancePrice = 0.12
    const bandwidthPrice = 0.25
    const total = calculateTotalPrice(instancePrice, bandwidthPrice)
    expect(total).toBe(0.37)
  })
})
```

## 🔨 开发说明

### 后端开发

```bash
# 运行测试
mvn test

# 打包
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests
```

### 前端开发

```bash
# 进入前端目录
cd frontend-elementplus

# 安装依赖
npm install

# 开发模式（热重载）
npm run dev

# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

### 代码规范

- 后端遵循 Java 编码规范
- 前端遵循 Vue 3 最佳实践
- 使用 Spring Boot 标准项目结构

## 📝 更新日志

### v0.0.1-SNAPSHOT

- 初始版本
- 实现基础功能：地域、可用区、实例规格、镜像查询
- 实现价格查询功能
- 实现实例列表查询
- 前端多步骤配置向导
- 支持配置模板导入/导出
- 多语言支持

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目采用 MIT 许可证。

## 🔗 相关链接

- [腾讯云 CVM 文档](https://cloud.tencent.com/document/product/213)
- [腾讯云 SDK Java](https://github.com/TencentCloud/tencentcloud-sdk-java)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [Vue 3 文档](https://vuejs.org/)
- [Element Plus 文档](https://element-plus.org/)

## 📧 联系方式

如有问题或建议，请通过 Issue 反馈。

---

**注意**：使用本工具需要有效的腾讯云账号和 API 凭证。请妥善保管您的凭证信息。


## 镜像筛选与推荐

后端统一处理镜像筛选与排序，前端只负责展示。

### 屏蔽关键词

镜像名或系统名包含以下关键词时会被过滤：

- `arm64`
- `aarch64`
- `tk4`
- `uefi`
- `tencentos`
- `grid`

> 匹配不区分大小写。

### 推荐排序规则

推荐排序按以下权重进行：

- `ubuntu`：+40
- `server`：+20
- `lts`：+20
- `20.04 / 22.04 / 24.04`：+10
- 公共镜像优先
- 创建时间较新的优先

### 前端展示

- 提供“仅看推荐镜像”开关
- 默认开启
- 如果推荐结果为空，则自动回退显示全部镜像

## 发布

推荐使用一键远端发布脚本：

```bash
bash <internal-deploy-script-removed>
```

默认流程：

1. 目标机拉取 `main` 分支
2. 构建前端
3. 构建后端 jar
4. 替换生产 jar
5. 重启并验活

默认目标：

- 主机：`ubuntu@<REDACTED_HOST>`
- 端口：`<REDACTED_PORT>`
- 生产 jar：`<REDACTED_DEPLOY_PATH>/tengxunCVM.jar`

约定：优先使用脚本发布，脚本失败后再手动排查。

