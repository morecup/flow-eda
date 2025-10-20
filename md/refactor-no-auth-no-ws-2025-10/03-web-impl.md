# Web 改造实施说明（保留状态接口，新增日志内容透传接口）

## 目标
- 保留流程状态/数据查询接口供轮询；
- 新增日志内容透传接口 `/api/v1/logs/content`；
- 清理 OAuth2 配置残留。

## 影响模块与文件
- 控制器：
  - `flow-eda-web/src/main/java/com/flow/eda/web/flow/status/FlowStatusController.java`（保留）
  - 新增 `LogContentController` 或在现有 `LogController` 中新增 `GET /api/v1/logs/content`，通过 Feign 调用 logger 的 `/api/v1/feign/logs/content`
- Feign：
  - 现有 `LogClient` 新增 `getLogContent(path)`
- 配置：
  - `application.yaml` 删除 oauth2 配置项（如 flow.oauth2.*）

## 实施步骤
1) 保留状态接口
- 无改动：
  - `GET /api/v1/feign/flow/status?flowId=...`
  - `GET /api/v1/feign/flow/data?flowId=...`

2) 新增日志内容透传
- 在 `LogClient` 中新增：
  - `@GetMapping("/api/v1/feign/logs/content") Result<String> getLogContent(@RequestParam String path);`
- 在 Web 侧控制器新增：
  - `@GetMapping("/api/v1/logs/content")` 调用 `logClient.getLogContent(path)` 并返回结果。

3) 清理 OAuth2 残留
- 删除 `application.yaml` 中 oauth2 配置；
- common 中保留占位类，避免编译影响。

4) 编译与验证
- `mvn -pl flow-eda-web -DskipTests=false clean package`
- 验证：
  - 前端可请求 `/api/v1/feign/flow/status` 获取状态；
  - `/api/v1/logs/content?path=...` 返回内容。

## 回归点
- 状态接口正常；
- 日志内容透传可用；
- 配置无 oauth2 相关项。
