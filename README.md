# 物流仓库管理系统 (WMS)

## How to Run

### 环境要求
- Docker 20.10+
- Docker Compose 2.0+

### 快速启动

1. 克隆项目到本地
```bash
git clone <repository-url>
cd label-01257
```

2. 使用 Docker Compose 启动所有服务
```bash
docker-compose up -d --build
```

3. 等待服务启动完成（约1-2分钟），访问系统
- 前端地址：http://localhost:8081
- 后端API：http://localhost:8080

4. 停止服务
```bash
docker-compose down
```

5. 清理数据重新开始
```bash
docker-compose down -v
docker-compose up -d --build
```

### 本地开发运行（不使用 Docker）

如果需要在本地进行开发调试，可以按以下步骤分别启动各个服务。

#### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- npm 9+ 或 pnpm
- MySQL 8.0+

#### 1. 数据库配置

1.1 安装并启动 MySQL 服务

1.2 执行初始化脚本（脚本会自动创建数据库）
```bash
# 使用 MySQL 命令行工具导入
mysql -u root -p < backend/src/main/resources/sql/schema.sql
```

1.4 修改数据库配置（如需要）

编辑 `backend/src/main/resources/application.yml`，修改数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wms_db?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
```

> 注：默认配置使用端口 3307，如果您的 MySQL 使用标准端口 3306，请相应修改

#### 2. 启动后端服务

```bash
# 进入后端目录
cd backend

# 安装依赖并编译
mvn clean install -DskipTests

# 启动后端服务
mvn spring-boot:run
```

或者使用 IDE（如 IntelliJ IDEA）直接运行 `WmsApplication.java` 主类。

后端服务启动后访问地址：http://localhost:9090

#### 3. 启动前端服务

```bash
# 进入前端目录
cd frontend-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端开发服务器启动后访问地址：http://localhost:3000

> 注：如果 3000 端口被占用，会自动使用 3001 端口。前端已配置代理，开发模式下 `/api` 请求会自动转发到后端 `http://localhost:9090`

#### 4. 前端生产构建

```bash
cd frontend-admin

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

构建产物位于 `frontend-admin/dist` 目录。

## Services

| 服务名称 | 端口 | 说明 |
|---------|------|------|
| frontend | 8081 | 前端管理界面 |
| backend | 8080 | 后端API服务 |
| mysql | 3307 | MySQL数据库 |

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 系统管理员 | admin | admin123 |
| 仓库管理员 | warehouse | admin123 |
| 操作员 | operator | admin123 |

> 注：所有测试账号初始密码均为 `admin123`，密码使用 BCrypt 加密存储

## 题目内容

开发一个功能完善的Java物流仓库管理系统，该系统需包含以下五大核心功能模块：库房管理、出入库管理、策略管理、运输调度管理和报表管理。采用Spring Boot 2.x作为后端框架，Vue作为前端框架，Maven作为项目构建工具，MySQL作为数据库存储方案。系统开发需同时完成前后端代码实现，并提供完整的数据库表结构创建SQL脚本。具体要求如下：1. 系统架构：采用前后端分离架构，后端提供RESTful API接口，前端负责用户界面展示与交互2. 功能模块详细实现：- 库房管理：实现库房信息维护、区域划分、货架管理、库存实时监控等功能- 出入库管理：支持入库登记、出库审核、库存调整、批次管理、扫码操作等功能- 策略管理：包含存储策略、拣货策略、补货策略等规则的配置与管理- 运输调度管理：实现运输任务创建、分配、跟踪、司机管理、车辆调度等功能- 报表管理：提供库存统计、出入库明细、周转率分析、异常预警等多维度报表3. 技术实现要求：- 后端：使用Spring Boot 2.x，集成Spring Security进行权限控制，MyBatis或JPA进行数据访问，实现事务管理、异常处理、日志记录等功能- 前端：基于Vue框架，使用Element UI或Ant Design Vue组件库，实现响应式布局，确保在不同设备上的良好显示效果- 数据库：设计合理的MySQL数据库表结构，包含用户表、角色表、权限表、库房表、商品表、库存表、出入库记录表等必要数据表4. 交付物：- 完整的前后端源代码，包含详细注释- 可执行的数据库表结构创建SQL脚本- 系统部署文档和使用说明- API接口文档系统应具备良好的可扩展性、可维护性和安全性，确保数据准确性和操作高效性。

---

## 项目介绍

本系统是一套完整的物流仓库管理系统（Warehouse Management System，简称WMS），采用前后端分离架构，提供仓库管理的全流程解决方案。

### 系统特点

- **模块化设计**：五大核心模块独立运作，便于维护和扩展
- **权限管理**：基于RBAC的权限控制，支持多角色多权限配置
- **实时监控**：库存实时监控，异常自动预警
- **数据报表**：多维度数据分析，支持导出功能
- **操作日志**：完整的操作审计追踪

### 功能模块

#### 1. 库房管理
- 库房信息维护（新增、编辑、删除、查询）
- 区域划分管理
- 货架管理
- 库存实时监控

#### 2. 出入库管理
- 入库登记与审核
- 出库申请与审核
- 库存调整
- 批次管理
- 出入库记录查询

#### 3. 策略管理
- 存储策略配置
- 拣货策略配置
- 补货策略配置
- 策略优先级管理

#### 4. 运输调度管理
- 运输任务创建与分配
- 任务状态跟踪
- 司机信息管理
- 车辆调度管理

#### 5. 报表管理
- 库存统计报表
- 出入库明细报表
- 周转率分析报表
- 异常预警报表
- 报表导出功能

### 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                    前端 (Vue 3 + Vite)                   │
│              Element Plus + Pinia + Axios               │
├─────────────────────────────────────────────────────────┤
│                         Nginx                           │
├─────────────────────────────────────────────────────────┤
│                后端 (Spring Boot 2.7.x)                  │
│     Spring Security + MyBatis-Plus + JWT                │
├─────────────────────────────────────────────────────────┤
│                    MySQL 8.0                            │
└─────────────────────────────────────────────────────────┘
```

### 项目结构

```
label-01257/
├── README.md                 # 项目说明文档
├── docker-compose.yml        # Docker Compose 配置
├── .gitignore               # Git 忽略文件
├── docs/                    # 文档目录
│   └── project_design.md    # 项目设计文档
├── backend/                 # 后端项目
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/wms/
│           │       ├── WmsApplication.java
│           │       ├── config/
│           │       ├── controller/
│           │       ├── service/
│           │       ├── mapper/
│           │       ├── entity/
│           │       ├── dto/
│           │       ├── vo/
│           │       ├── common/
│           │       └── utils/
│           └── resources/
│               ├── application.yml
│               ├── mapper/
│               └── sql/
└── frontend-admin/          # 前端项目
    ├── Dockerfile
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── api/
        ├── assets/
        ├── components/
        ├── router/
        ├── stores/
        ├── styles/
        ├── utils/
        └── views/
```
