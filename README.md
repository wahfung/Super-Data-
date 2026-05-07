# Super Data 数据管理系统

一个基于 SpringBoot + Vue3 + ElementPlus 的现代化数据管理系统，具备完整的 RBAC 权限管理和双重认证机制。

## 🛠 技术栈

| 层级   | 技术选型                                                        |
| ------ | --------------------------------------------------------------- |
| 前端   | Vue 3 + Vite + TypeScript + ElementPlus + Pinia                 |
| 后端   | Java 17 + SpringBoot 2.7 + Spring Security + JWT + MyBatis-Plus |
| 数据库 | MySQL 8.0                                                       |

## 🚀 启动指南

### 前置条件

- Java 17 已安装
- Node.js 20+ 已安装
- MySQL 8.0 已安装并运行
- 端口 8080, 3306 未被占用

### 后端启动

```bash
cd backend

# 修改 application.yml 中的数据库连接配置
mvn spring-boot:run
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器运行在 http://localhost:5173，会自动代理 API 请求到后端。

## 🔗 服务地址

| 服务                  | 地址                                                  |
| --------------------- | ----------------------------------------------------- |
| 前端界面              | http://localhost:3000                                 |
| 后端 Swagger API 文档 | http://localhost:8080/swagger-ui.html                 |
| 数据库                | localhost:3306 (user: superdata / pass: superdata123) |

## 🧪 测试账号

| 账号  | 密码   | 角色       | 说明         |
| ----- | ------ | ---------- | ------------ |
| admin | 123456 | 超级管理员 | 拥有所有权限 |
| user1 | 123456 | 普通用户   | 仅有查看权限 |
| user2 | 123456 | 普通用户   | 仅有查看权限 |

### API 客户端 (Basic Auth)

```bash
# 测试对外 API
curl -u open-api-client:123456 http://localhost:8080/open-api/v1/health

# 获取用户列表
curl -u open-api-client:123456 http://localhost:8080/open-api/v1/users
```

## 📋 功能特性

### 认证机制

- **JWT Token 认证**: 用于内部管理接口 (`/api/**`)
  - 登录获取 accessToken + refreshToken
  - Token 自动刷新机制

- **Basic Auth 认证**: 用于对外开放接口 (`/open-api/**`)
  - 使用 API 客户端凭证认证
  - 适用于第三方系统集成

### 权限管理

- **RBAC 权限模型**: 用户 → 角色 → 权限
- **接口级权限控制**: 基于 Spring Security `@PreAuthorize`
- **前端路由守卫**: 根据用户权限动态显示菜单
- **按钮级权限**: 根据权限显示/隐藏操作按钮

### 系统功能

| 模块     | 功能                          |
| -------- | ----------------------------- |
| 仪表盘   | 系统概览、统计数据、快速入口  |
| 用户管理 | 用户 CRUD、角色分配、状态切换 |
| 角色管理 | 角色 CRUD、权限分配           |
| 权限管理 | 权限 CRUD                     |

## 🗄️ 数据库设计

### 核心表结构

| 表名                | 说明                      |
| ------------------- | ------------------------- |
| sys_user            | 用户表                    |
| sys_role            | 角色表                    |
| sys_permission      | 权限表                    |
| sys_user_role       | 用户角色关联表            |
| sys_role_permission | 角色权限关联表            |
| sys_api_client      | API 客户端表 (Basic Auth) |

## 📁 项目结构

```
repo/
├── README.md                   # 项目说明
├── backend/                    # SpringBoot 后端
│   ├── pom.xml
│   ├── settings.xml            # Maven 阿里云镜像
│   └── src/main/
│       ├── java/com/superdata/
│       │   ├── config/         # 配置类
│       │   ├── security/       # 安全模块
│       │   ├── controller/     # 控制器
│       │   ├── service/        # 业务逻辑
│       │   ├── mapper/         # MyBatis Mapper
│       │   ├── entity/         # 实体类
│       │   ├── dto/            # 数据传输对象
│       │   ├── common/         # 公共模块
│       │   └── exception/      # 异常处理
│       └── resources/
│           └── application.yml
├── frontend/                   # Vue 3 前端
│   ├── nginx.conf
│   ├── package.json
│   └── src/
│       ├── api/                # API 请求
│       ├── components/         # 公共组件
│       ├── router/             # 路由配置
│       ├── stores/             # Pinia 状态管理
│       ├── types/              # TypeScript 类型
│       ├── utils/              # 工具函数
│       └── views/              # 页面组件
└── db/
    └── init.sql                # 数据库初始化脚本
```

## 🔧 镜像源配置

本项目已配置以下镜像源加速：

- **Maven**: 阿里云镜像 (backend/settings.xml)

## 📝 API 文档

启动后端服务后，访问 Swagger UI 查看完整 API 文档：

http://localhost:8080/swagger-ui.html

### 主要 API 端点

| 方法 | 路径                | 说明            | 认证方式   |
| ---- | ------------------- | --------------- | ---------- |
| POST | /api/auth/login     | 用户登录        | 无         |
| GET  | /api/auth/me        | 获取当前用户    | JWT        |
| GET  | /api/users          | 用户列表        | JWT        |
| GET  | /api/roles          | 角色列表        | JWT        |
| GET  | /api/permissions    | 权限列表        | JWT        |
| GET  | /open-api/v1/health | 健康检查        | Basic Auth |
| GET  | /open-api/v1/users  | 用户列表 (对外) | Basic Auth |

## ⚠️ 注意事项

1. 首次启动时，MySQL 需要初始化，可能需要等待 30 秒左右
2. 生产环境请修改数据库密码和 JWT 密钥
3. 所有接口采用 UTF-8 编码，确保中文正常显示

## 📄 License

MIT License
