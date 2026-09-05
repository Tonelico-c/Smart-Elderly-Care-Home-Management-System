# 智慧养老院管理系统（elder）

面向养老院场景的一仓三端管理系统：**Spring Boot 后端** + **Vue 3 管理后台**（工作人员使用）+ **Vue 3 老人端 App**（老人使用），并基于 Spring AI 接入大模型，提供 AI 健康咨询与体检报告智能解读。

覆盖老人档案、住宿（楼栋/房间/床位）、入住办理、护理（等级/项目/计划/任务）、健康体检（项目/套餐/预约/结果）、请假审批等业务闭环。

## 一、技术栈

### 后端（`src/`）

| 技术 | 说明 |
| ---- | ---- |
| Spring Boot 3.4.5 | 主体框架，Java 21 |
| MyBatis-Plus 3.5.5 | ORM：代码生成器（`MPGenerator`）、分页插件、逻辑删除、字段自动填充 |
| MySQL 8 | 数据库，库名 `elder`，22 张业务表 |
| JWT（java-jwt 4.4） | 双端登录认证：拦截器统一校验，App 端从 token 解析老人 ID |
| Spring AI 1.1 + 阿里百炼 | 大模型 `qwen-plus`：系统提示词约束、会话记忆隔离、Function Calling、SSE 流式输出 |
| Apache POI / EasyExcel | Excel 导入导出 |
| 阿里云 OSS | 头像等文件上传（AccessKey 从环境变量读取） |
| Lombok / Logback | 简化实体类 / 日志 |

### 管理后台（`ui/ui-admin/`，端口 5173）

| 技术 | 说明 |
| ---- | ---- |
| Vue 3.5 + Vite | 前端框架与构建工具 |
| Element Plus 2.14 | UI 组件库 |
| Vue Router / Pinia | 路由 / 状态管理（持久化插件保存登录态） |
| Axios | 统一封装：自动携带 token、`Result` 解包、401 拦截跳登录 |

### 老人端 App（`ui/ui-app/`，端口 5174）

| 技术 | 说明 |
| ---- | ---- |
| Vue 3.5 + Vite | 前端框架与构建工具 |
| Vant 4 | 移动端 UI 组件库（适老化交互） |
| Pinia / Axios | 同上，与管理后台各自独立封装 |

## 二、功能模块

### 管理后台（`controller/admin` → `/admin/**`）

- **权限体系**：用户 / 角色 / 权限管理，角色分配权限（树形结构）；前端按角色动态渲染菜单，按钮级权限控制（`btnPermission`）
- **老人管理**：老人档案 CRUD、标签分配、联表查询（LEFT JOIN + GROUP_CONCAT 一次带出标签）、Excel 导出
- **住宿管理**：楼栋 → 房间 → 床位三级管理，房间详情查看床位入住情况，统计卡片
- **入住办理**：选择老人分配床位生成入住记录，支持换房（换房间或床位）
- **护理管理**：护理等级、护理项目、护理计划（计划可包含多个项目）、护理任务派发与执行；护工角色登录后仅可见分配给自己的任务
- **体检管理**：体检项目、体检套餐（穿梭框分配项目）、体检预约与结果录入（数值/文本双类型，录入完成自动结单）
- **请假管理**：审批老人的请假申请，实现请/销假流程；审批通过后老人、入住记录、床位状态在同一事务中联动，请假不释放床位
- **首页工作台**：数据统计展示
- **通用**：文件上传（OSS）、Excel 导入导出

### 老人端 App（`controller/app` → `/app/**`）

- **登录**：手机号登录，JWT 持久化，可修改密码
- **体检**：浏览体检套餐、预约体检、查看我的预约与已完成体检报告
- **AI 智能咨询**：与 AI 健康助手"康养小智"流式对话，AI 可调用工具查询老人信息和最近体检结果
- **AI 体检分析**：一键让 AI 解读体检报告，并可在对话中继续追问
- **请假**：提交请假申请（外出/返回时间两步选择）、查看请假记录与审批进度
- **个人中心**：查看个人信息

### AI 能力（Spring AI）

- **角色与安全边界**：系统提示词设定 AI 为养老健康助手，不替代医生诊断、紧急情况引导就医
- **会话记忆隔离**：`PromptChatMemoryAdvisor` 以老人 ID 作为 conversationId，不同用户上下文互不串扰
- **工具调用（Function Calling）**：`@Tool` 封装 `ElderTools` / `ExamAppointmentTools` / `TimeTools`，模型按需自主调用查询真实数据，避免幻觉
- **流式输出**：`Flux<String>` + SSE 推送，前端 `fetch` + ReadableStream 手写 SSE 解析、逐字渲染，失败时兜底提示

## 三、架构说明

- **分层**：`controller/{admin,app}` → `service/I*Service + impl` → `mapper`（复杂 SQL 写在 `resources/mapper/*.xml`）；POJO 按 `entity / query / vo / dto` 分目录
- **双端复用**：admin / app 控制器分目录、分路径前缀，共用同一批 service；拦截器按前缀放行各自的登录接口
- **统一返回**：`Result` 封装（`code: 1=成功 / 0=失败`）；业务异常抛 `ServiceException`，`GlobalExceptionHandler` 统一处理
- **认证**：JWT 裸 token 放 `Authorization` 头；`LoginInterceptor` 统一校验，未登录返回 401，前端据此跳转登录页
- **MyBatis-Plus 约定**：逻辑删除字段 `deleted`；`create_time` / `update_time` 由 `MyMetaObjectHandler` 自动填充；分页使用 `IPage`
- **日期处理**：`WebConfig` 注册 `String → Date` 转换器（支持 `yyyy-MM-dd HH:mm:ss`、`yyyy-MM-dd` 及 ISO 格式）；Jackson 全局日期格式 `yyyy-MM-dd HH:mm:ss`（GMT+8）
- **前后端联调**：两份 `vite.config.js` 的开发代理将 `/api` 重写转发到 `http://localhost:8080`（如 `/api/admin/elders` → 后端 `/admin/elders`）

## 四、快速启动

### 准备

- JDK 21、Node.js、MySQL 8
- 创建数据库 `elder` 并导入表结构

### 后端（端口 8080）

1. 修改 `src/main/resources/application.yml` 中的数据库账号密码
2. 设置环境变量 `DASHSCOPE_API_KEY`（阿里百炼 API Key，AI 功能依赖它）
3. 启动：

```bash
./mvnw spring-boot:run        # Windows Git Bash；cmd 下用 mvnw.cmd
```

### 管理后台（端口 5173）

```bash
cd ui/ui-admin
npm install
npm run dev
```

### 老人端 App（端口 5174）

```bash
cd ui/ui-app
npm install
npm run dev
```

## 五、项目结构

```
elder
├── src/main/java/com/situ/elder
│   ├── controller
│   │   ├── admin            # 管理后台接口（/admin/**，23 个控制器）
│   │   └── app              # 老人端 App 接口（/app/**：登录/体检/请假/AI 对话）
│   ├── service              # 业务接口与实现（两端复用）
│   ├── mapper               # MyBatis-Plus Mapper
│   ├── pojo                 # entity 实体 / query 查询对象 / vo 视图对象 / dto 传输对象
│   ├── tools                # AI 工具（@Tool，供大模型 Function Calling 调用）
│   ├── config               # Agent / MyBatis-Plus / Jackson / Web 配置
│   ├── interceptor          # 登录拦截器
│   ├── exception            # 业务异常 + 全局异常处理
│   ├── utils                # JwtUtil / ExcelUtil / AliOSSUtil / Result
│   ├── excelListener        # EasyExcel 导入监听器
│   └── MPGenerator.java     # MyBatis-Plus 代码生成器（改 dbTables 后运行 main）
├── src/main/resources
│   ├── application.yml      # 应用配置
│   └── mapper               # 联表查询 XML（22 个）
├── ui/ui-admin              # 管理后台（Vue 3 + Element Plus）
│   └── src/views            # 21 个页面（登录/工作台/老人/住宿/护理/体检/请假/权限…）
├── ui/ui-app                # 老人端 App（Vue 3 + Vant 4）
│   └── src/views            # 12 个页面（登录/体检/请假/AI 咨询/个人中心）
└── pom.xml
```
