<p align="center">
  <h1 align="center">Picture</h1>
  <p align="center">画图协作平台 — 支持图片上传、管理、实时协作编辑</p>
</p>

## 功能特性

- **用户系统** — 注册 / 登录、个人信息管理、VIP 兑换码升级
- **图片管理** — 文件上传（支持拖拽）、URL 抓取、批量导入、以图搜图
- **空间管理** — 私有空间与团队空间，支持三级套餐（普通版 / 专业版 / 旗舰版），不同容量与数量上限
- **权限控制** — 基于 Sa-Token 的空间角色权限（管理员 / 编辑者 / 查看者），方法级权限注解
- **协作编辑** — WebSocket + Disruptor 实现实时图片编辑，排他锁机制保证编辑互斥
- **空间分析** — 使用量、分类、标签、大小分布、用户行为等多维度图表分析（ECharts）
- **图片处理** — 上传自动转 WebP 格式，图片裁剪

## 技术栈

| 层级 | 技术 |
|------|------|
| **后端框架** | Spring Boot 2.7.6 + Java 8 |
| **ORM** | MyBatis-Plus 3.5.9 |
| **权限认证** | Sa-Token + 自定义空间权限 |
| **数据库** | MySQL + ShardingSphere 5.2.0（动态分表） |
| **缓存** | Redis + Caffeine 两级缓存 |
| **消息队列** | LMAX Disruptor（WebSocket 事件处理） |
| **对象存储** | 腾讯云 COS（万象图片处理） |
| **API 文档** | Knife4j / Swagger |
| **前端框架** | Vue 3 + TypeScript + Vite |
| **UI 组件** | Ant Design Vue 4 |
| **图表** | ECharts 6 + vue-echarts |
| **状态管理** | Pinia |

## 项目结构

```
picture/
├── src/main/java/com/suny/picture/
│   ├── annotation/          # 自定义注解（权限校验）
│   ├── aop/                 # AOP 拦截器
│   ├── api/                 # 第三方 API 封装（以图搜图）
│   ├── common/              # 通用响应类
│   ├── config/              # Spring 配置
│   ├── constant/            # 常量
│   ├── controller/          # 控制器层
│   ├── exception/           # 异常处理
│   ├── manager/             # 可复用管理器
│   │   ├── auth/            # 空间权限管理
│   │   ├── sharding/        # 动态分表
│   │   ├── upload/          # 图片上传策略
│   │   └── websocket/       # WebSocket 实时编辑
│   ├── mapper/              # MyBatis Mapper
│   ├── model/               # 数据模型
│   │   ├── dto/             # 请求 DTO
│   │   ├── entity/          # 数据库实体
│   │   ├── enums/           # 枚举
│   │   └── vo/              # 视图对象
│   └── service/             # 业务逻辑层
├── src/main/resources/
│   ├── mapper/              # MyBatis XML
│   ├── sql/                 # 建表 SQL
│   ├── biz/                 # 权限配置文件
│   └── application.yml      # 主配置
├── frontend/                # 前端项目
│   └── src/
│       ├── api/             # API 接口
│       ├── components/      # 通用组件
│       ├── pages/           # 页面
│       ├── router/          # 路由
│       ├── stores/          # 状态管理
│       └── utils/           # 工具函数
└── pom.xml
```

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Node.js 20.19+ / 22.12+

### 后端启动

```bash
# 1. 克隆项目
git clone https://github.com/suny1798/Picture.git
cd Picture

# 2. 配置数据库
# 创建数据库，执行 src/main/resources/sql/ 下的建表脚本
# 复制并修改 application-local.yml 中的数据库连接信息

# 3. 启动
mvn spring-boot:run
# 服务运行在 http://localhost:8080/api
# API 文档: http://localhost:8080/api/doc.html
```

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 开发模式
npm run dev
# 访问 http://localhost:5173
# 取消 vite.config.ts 中 proxy 注释可将请求代理到后端
```

## API 接口

| 模块 | 端点 | 说明 |
|------|------|------|
| 用户 | `/api/user/*` | 注册、登录、信息管理、VIP 兑换 |
| 图片 | `/api/picture/*` | 上传、编辑、删除、审核、搜索 |
| 空间 | `/api/space/*` | 创建、编辑、删除空间 |
| 空间用户 | `/api/spaceUser/*` | 成员管理、角色分配 |
| 空间分析 | `/api/space/analyze/*` | 使用量、分类、标签等分析 |
| 文件 | `/api/file/*` | 文件上传 |
| WebSocket | `ws://host/api/ws/picture/edit` | 实时协作编辑 |

## 空间套餐

| 套餐 | 图片数量 | 总容量 | 分表支持 |
|------|----------|--------|----------|
| 普通版 | 100 | 100 MB | - |
| 专业版 | 1,000 | 1 GB | - |
| 旗舰版 | 10,000 | 10 GB | 团队空间分表 |
