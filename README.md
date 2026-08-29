# 智慧养老院管理系统（elder）

一个养老院后台管理系统，前后端分离架构。后端提供 REST API，前端为基于 Vue 3 + Element Plus 的管理后台。

> **项目状态：开发中。** 核心的登录认证与基础 CRUD 已完成，部分业务模块（见下文"待完成功能"）尚未实现。

## 一、技术栈

### 后端（`src/`）

| 技术 | 说明 |
| ---- | ---- |
| Spring Boot 3.4.5 | 主体框架，Java 21 |
| MyBatis-Plus 3.5.5 | ORM，含代码生成器（MPGenerator）、逻辑删除、分页插件 |
| MySQL 8 | 数据库，库名 `elder` |
| JWT（java-jwt 4.4.0） | 登录认证，拦截器校验 Token |
| EasyExcel 3.1.1 | Excel 导入导出 |
| 阿里云 OSS | 头像等文件上传 |
| Lombok / Logback | 简化实体类 / 日志 |

### 前端（`ui/ui-admin/`）

| 技术 | 说明 |
| ---- | ---- |
| Vue 3.5 + Vite 8 | 前端框架与构建工具 |
| Element Plus 2.14 | UI 组件库（图标全局注册） |
| Vue Router 5 / Pinia 4 | 路由 / 状态管理（pinia-plugin-persistedstate 持久化） |
| Axios | HTTP 请求 |

## 二、功能模块

### 已完成

**1. 用户管理（`/users`）**
- 登录认证：校验用户名、密码、账号禁用状态，成功后签发 JWT Token
- 用户 CRUD：分页 + 多条件查询（姓名、邮箱、创建时间区间）、新增（用户名唯一校验）、修改、删除（单条/批量，逻辑删除）
- 当前用户信息查询（Token 解析）、修改密码（校验原密码）
- 角色分配：查询用户已分配角色回显、全量覆盖式重新分配
- Excel 导出（全部用户）、Excel 导入（EasyExcel 监听器逐行入库）

**2. 老人管理（`/elders`）**
- 老人 CRUD：分页 + 多条件查询（姓名、电话、创建时间区间）、增、删（单条/批量，逻辑删除）、改、查
- 标签分配：查询老人已分配标签回显、全量覆盖式重新分配
- 列表联表查询：一条 SQL（LEFT JOIN + `GROUP_CONCAT`）同时带出每个老人的标签名串
- Excel 导出：含状态码翻译为中文备注（禁用/启用/请假/退住中/入住中/已退住）、出生日期格式化为 `yyyy-MM-dd`

**3. 标签管理（`/tags`）**
- 标签 CRUD：分页查询、增、删、改

**4. 角色管理（`/roles`）**
- 角色 CRUD：分页 + 多条件查询（角色名、编码、创建时间区间）
- 查询角色关联的权限（分页）

**5. 权限管理（`/permissions`）**
- 权限 CRUD：增、删（单条/批量）、改、查
- 递归构建权限树形结构（供角色分配权限时渲染树形控件）

**6. 通用能力**
- 统一响应封装（`Result`）与全局异常处理（`GlobalExceptionHandler`）
- 登录拦截器（`LoginInterceptor`）校验 Token
- 文件上传（阿里云 OSS，`/upload`）
- MyBatis-Plus 自动填充（创建时间/更新时间，`MyMetaObjectHandler`）

### 待完成 / 规划中

- 角色分配权限的保存接口完善与前端页面联调
- 基于角色/权限的动态菜单与按钮级权限控制（后端权限校验目前仅到登录拦截）
- 密码加密存储（当前为明文比对，待引入 BCrypt 等哈希方案）
- 老人业务扩展：床位/寝室管理、健康档案、请假/退住流程等
- 前端其余页面与后端接口的完整联调

### 规划中：护理工作管理（未开始）

按老人的护理等级制定护理计划，计划中安排具体的护理服务项目。涉及的表（初步设计）：

| 表 | 说明 |
| ---- | ---- |
| `care_level` | 护理等级：老人属于什么护理等级（如自理、半护理、全护理） |
| `care_plan` | 护理计划：这个老人具体怎么护理 |
| `care_item` | 护理服务项目：可以提供哪些护理服务，如：测量血糖、测量体温、协助吃饭、协助洗澡、协助如厕、协助起床、协助服药、康复训练、心理陪护、房间清洁 |
| `care_plan_item` | 护理计划明细：这个老人的护理计划具体安排了哪些服务以及执行规则（如频次、时间段等） |

## 三、快速启动

### 后端

1. 创建数据库并导入表结构（库名 `elder`，MySQL 8）
2. 修改 `src/main/resources/application.yml` 中的数据库账号密码
3. 启动主类 `com.situ.elder.ElderApplication`

### 前端

```bash
cd ui/ui-admin
npm install
npm run dev
```

## 四、项目结构

```
elder
├── src/main/java/com/situ/elder
│   ├── controller      # REST 接口层（User/Elder/Tag/Role/Permission 等）
│   ├── service         # 业务接口与实现
│   ├── mapper          # MyBatis-Plus Mapper
│   ├── pojo            # entity 实体 / query 查询对象 / vo 视图对象 / dto 传输对象
│   ├── interceptor     # 登录拦截器
│   ├── config          # MyBatis-Plus、Jackson、Web 配置
│   ├── exception       # 业务异常 + 全局异常处理
│   ├── utils           # JwtUtil / ExcelUtil / AliOSSUtil / Result
│   └── excelListener   # EasyExcel 导入监听器
├── src/main/resources
│   ├── application.yml # 应用配置
│   └── mapper          # XML 映射文件
├── ui/ui-admin         # Vue 3 + Element Plus 前端管理后台
│   └── src/views       # Login / Index / User / Elder / Tag / Role / Permission / UserInfo
└── pom.xml
```
