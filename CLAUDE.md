# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在本仓库中工作时提供指引。

## 项目概述

智慧养老院管理系统。项目注释、提交信息、界面文案均使用中文，请遵循该约定。

仓库内包含三个独立运行的部分：

- **后端**：Spring Boot 3.4.5 / Java 21 / MyBatis-Plus 3.5.5 / MySQL 8（`src/`）
- **管理后台**：Vue 3.5 + Element Plus + Pinia（`ui/ui-admin/`），开发端口 5173
- **老人端 App**：Vue 3.5 + Vant 4 + Pinia（`ui/ui-app/`），开发端口 5174

## 常用命令

### 后端（在仓库根目录执行）

```bash
./mvnw spring-boot:run        # 启动，端口 8080（Windows Git Bash；cmd 下用 mvnw.cmd）
./mvnw package                # 打包 jar（跳过测试也没关系——没有真正的测试套件）
./mvnw test                   # src/test 下只有演示/练习类，不是真实测试
```

需要本地 MySQL 8 中存在数据库 `elder`（账号密码见 `src/main/resources/application.yml`）。

### 前端

```bash
cd ui/ui-admin && npm install && npm run dev    # 管理后台，端口 5173
cd ui/ui-app   && npm install && npm run dev    # 老人端 App，端口 5174
```

### 代码生成

`src/main/java/com/situ/elder/MPGenerator.java` — MyBatis-Plus 代码生成器。修改 `dbTables` 常量为目标表名（逗号分隔），然后运行其 `main()`，即可从数据库表生成 entity/mapper/service/controller 脚手架。新增模块请沿用其生成的目录结构。

## 架构

### 后端分层（每个业务模块一致）

`controller/{admin,app}` → `service/I*Service` + `service/impl/*ServiceImpl` → `mapper/*Mapper`（超出 MyBatis-Plus wrapper 能力的 SQL 写在 `src/main/resources/mapper/*.xml`）。

- 控制器按使用方拆分：`controller/admin/*` 服务管理后台（路径前缀 `/admin/...`），`controller/app/*` 服务老人端 App（路径前缀 `/app/...`）。两侧通常复用同一批 service。
- POJO 按用途放在 `pojo/` 下：`entity`（数据库实体）、`query`（列表查询条件，分页需继承 `PageQuery`）、`vo`（联表/计算后的返回视图）、`dto`（App 端传输对象）。
- 所有接口统一返回 `Result` 封装（`utils/Result.java`）：`{code: 1=成功 / 0=失败, msg, data}`。业务异常抛 `ServiceException`，由 `GlobalExceptionHandler` 统一处理。

### 认证

基于 JWT。`LoginInterceptor`（在 `WebConfig` 中注册）拦截所有请求，仅放行 `/admin/users/login` 和 `/app/elders/login`。前端传的是裸 token，放在 `Authorization` 请求头中（无 "Bearer " 前缀）。App 端控制器常用 `@RequestHeader("Authorization") String token` 接收，再通过 `JwtUtil` 解析出老人 id。未登录请求返回 401，前端据此跳转登录页。

### 前后端联调

- 两个前端各自的 axios 实例（`src/utils/request.js`）设置 `baseURL` 为 `/api/admin` 或 `/api/app`，并从 Pinia 持久化的 `token` store 中取 token 放入请求头。响应拦截器对 `Result` 解包（直接返回 `response.data`，因此调用方拿到的是 `Result` 对象而非 Axios 响应）。
- 两份 `vite.config.js` 中的开发代理会把 `/api` 重写并转发到 `http://localhost:8080`，即 `/api/admin/elders` 实际请求后端 `/admin/elders`。
- API 模块放在 `src/api/*.js`（与后端模块一一对应）；页面在 `src/views/*.vue`，路由在 `src/router/index.js` 中静态注册。
- `ui-admin` 的按钮级权限工具在 `src/utils/btnPermission.js`。

### MyBatis-Plus 约定

- 逻辑删除字段为 `deleted`（1 = 已删除，0 = 未删除）——`removeById`/`removeBatchByIds` 执行的是软删除。
- `create_time`/`update_time` 由 `MyMetaObjectHandler` 自动填充，不要手动赋值。
- 分页使用 MyBatis-Plus 的 `IPage`，分页插件在 `MybatisPlusConfig` 中配置。
- 需要联表数据的列表接口用手写 XML：`LEFT JOIN` + `GROUP_CONCAT`（参考 `ElderMapper.xml` / `GROUP_CONCAT用法.md`）。

### 日期处理

`WebConfig` 注册了 `String → Date` 转换器，支持 `yyyy-MM-dd HH:mm:ss`、`yyyy-MM-dd` 及 ISO 格式——查询参数和 JSON 请求体中的这些格式可直接绑定。Jackson 全局日期格式为 `yyyy-MM-dd HH:mm:ss`（GMT+8）。
