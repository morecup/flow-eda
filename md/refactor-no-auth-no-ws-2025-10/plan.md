# Flow-EDA 重构方案：移除 OAuth2/登录/用户态 与 WebSocket（前端短轮询替代）

## 背景与目标
- 彻底移除：OAuth2 模块、登录/注册、用户态、所有 WebSocket（前后端、节点类型、网关）。
- 前端以短轮询替代实时能力：
  - 流程运行状态：1s 轮询后端 REST。
  - 日志内容：2s 轮询后端 REST。
- 内部系统：无需鉴权与灰度，直接发布；保留 minimal 占位类以兼容编译。

## 范围与影响面
- 删除模块与依赖：
  - 移除子模块 flow-eda-oauth2（根 pom.xml modules 排除）
  - runner/logger 移除 `spring-boot-starter-websocket` 依赖
  - 删除所有 `@ServerEndpoint`/WebSocket 相关类
- 删除功能与节点：
  - 删除 ws_server/ws_client 节点实现与 NodeTypeEnum 枚举项
  - 删除 SQL 示例与文档中 WebSocket 相关内容（不改 README，除非后续要求）
- 前端：
  - 删除 Login/Register 视图与 oauth2 API
  - 删除 utils/websocket.js 与 api/ws.js
  - Editor/Logs 改短轮询；vite.config.js 去除 /ws 代理
- 网关/部署：
  - 去除 /ws/* 代理路由（如存在）

## 后端改造设计
### flow-eda-runner
1) 移除 WS 依赖与类
- 删除依赖：`spring-boot-starter-websocket`
- 删除类：`status/FlowNodeWebsocket.java`、`logger/ws/*`（如引用）
- FlowDataRuntime.stop/clear 不再通过 WS 推送中断/完成，仅写状态与 MQ

2) FlowExecutor 推送改造
- 原：通过 FlowNodeWebsocket 即时推送节点状态
- 现：仅调用 FlowStatusService 维护 runningMap，最终状态变化通过 FlowStatusMqProducer 发布到 RabbitMQ；由 web 侧持久化/查询

3) 删除 WS 节点类型
- NodeTypeEnum 移除 `WS_SERVER`、`WS_CLIENT`
- 删除对应包 `node/ws/**`

### flow-eda-logger
1) 移除 WS 依赖与类
- 删除依赖：`spring-boot-starter-websocket`
- 删除类：`ws/FlowLogWebsocket.java`、`ws/LogContentWebsocket.java`

2) 新增 REST 接口（供轮询）
- GET `/api/v1/feign/logs`：保留（已有）
- DELETE `/api/v1/feign/logs`：保留（已有）
- GET `/api/v1/feign/logs/content?path=...`：读取日志文件内容（全量/增量按实现选择，首版全量）

### flow-eda-web
1) FlowStatusController 保留
- GET `/api/v1/feign/flow/status?flowId=...`：获取流程状态（前端 1s 轮询）
- GET `/api/v1/feign/flow/data?flowId=...`：获取流程运行节点数据（如需）

2) Logs 透传接口
- 新增 GET `/api/v1/logs/content?path=...`：调用 logger 的 `/api/v1/feign/logs/content`

3) 清理 OAuth2 占位
- 保留 common 中占位类（不影响编译），移除 web 配置中 oauth2 相关项

## 前端改造设计（flow-eda-vue）
1) 删除文件与路由
- 删除 `src/views/Login.vue`、`src/views/Register.vue`
- 删除 `src/api/oauth2.js`
- 删除 `src/utils/websocket.js`、`src/api/ws.js`
- 清理路由守卫中与登录相关逻辑

2) Editor 页面（流程状态）
- 去除 WS 连接代码；新增 `setInterval` 每 1000ms 调用：
  - `/api/v1/feign/flow/status?flowId=...` 更新 `flowStatus`
  - 可选：周期性拉取 `/api/v1/feign/flow/data?flowId=...` 更新节点状态/输出（首版可仅显示流程级状态）

3) Logs/LogDetail 页面（日志内容）
- 2s 轮询 `/api/v1/logs/content?path=...`，将结果追加显示

4) vite.config.js
- 移除 `^/ws/*` 代理，保留 `/api/v1` 代理到网关/web

## 配置与数据
- application.yaml 中 oauth2 配置删除（必要时留空占位）
- nacos/gateway 配置删除 WS 路由
- SQL 示例（flow_eda.sql）删除 ws 节点类型与示例流程

## 迁移步骤
1) 代码层：后端按模块删除 WS 依赖与类；Runner 调整 FlowExecutor；Logger 增加 content 接口；Web 增加 content 透传接口
2) 前端：删除鉴权与 WS 代码；改用轮询；移除 ws 代理
3) 构建与验证：
   - 后端：`mvn -DskipTests=false clean package`
   - 前端：`npm i && npm run build`
   - 回归：流程运行/停止；状态变更；日志列表/内容
4) 部署：内部系统直接替换并启动

## 回滚
- 保留当前分支变更；如需回滚，恢复至变更前 tag/commit；无需配置切换。

## 安全与合规
- 内部系统，无鉴权；若未来开放外部，建议最小限度：IP 白名单/签名/限流。

## 验收标准
- 无任何 WebSocket 依赖与代码
- 前端不出现登录/注册入口与相关资源
- 流程状态在 1s 粒度可见，日志内容可在 2s 粒度刷新
- 流程运行全链路可用，日志列表/内容可读，可删除
