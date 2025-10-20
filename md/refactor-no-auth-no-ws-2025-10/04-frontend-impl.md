# 前端改造实施说明（删除登录与 WebSocket，短轮询替代）

## 目标
- 删除登录/注册与 OAuth2 相关代码；
- 删除所有 WebSocket 相关工具与调用；
- Editor 使用 1s 轮询流程状态；Logs/LogDetail 使用 2s 轮询日志内容；
- 移除 Vite 中 `/ws/*` 代理项。

## 影响模块与文件
- 删除：
  - `src/views/Login.vue`、`src/views/Register.vue`
  - `src/api/oauth2.js`
  - `src/utils/websocket.js`、`src/api/ws.js`
- 修改：
  - `src/views/Editor.vue`：移除 WS；新增 `setInterval` 轮询 `/api/v1/feign/flow/status`（必要时 `/flow/data`）
  - `src/views/Logs.vue`、`src/views/LogDetail.vue`：2s 轮询 `/api/v1/logs/content?path=...`
  - `vite.config.js`：删除 `^/ws/flow`、`^/ws/logs` 代理
  - 路由守卫：删除与登录相关的拦截逻辑

## 实施步骤
1) 清理登录/Auth 代码
- 删除 Login/Register 视图与路由条目；
- 删除 `src/api/oauth2.js`；
- 移除任何对 `userLogin/userRegister` 的引用。

2) 移除 WebSocket
- 删除 `src/utils/websocket.js`、`src/api/ws.js`；
- 在 Editor/Logs/LogDetail 中移除所有 onOpen/onClose 等调用。

3) 增加轮询
- Editor：`setInterval(()=> fetch(/api/v1/feign/flow/status), 1000)` 更新 `flowStatus`；
- Logs/LogDetail：`setInterval(()=> fetch(/api/v1/logs/content?path=...), 2000)` 追加渲染。

4) 调整 Vite 代理
- 删除：
  - `"^/ws/flow"` 与 `"^/ws/logs"` 两个代理条目。

5) 构建与验证
- `npm i && npm run dev` 验证开发态；`npm run build` 产物构建。

## 回归点
- 页面无登录/注册入口与资源 404；
- Editor 状态每秒更新；
- Logs/LogDetail 能持续刷新日志；
- 开发代理不再含 WS 项。

## TDD 测试策略
1) 组件级测试（可选，若项目集成了测试框架）
- Editor：mock request 层，验证 1s 轮询触发与状态更新；
- Logs/LogDetail：mock 日志内容接口，验证 2s 轮询并追加渲染；

2) e2e（可选）
- 启动后端 mock 或 dev 服务，使用 Cypress/Playwright 验证交互与轮询更新。
