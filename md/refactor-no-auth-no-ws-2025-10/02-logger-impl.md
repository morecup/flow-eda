# Logger 改造实施说明（移除 WebSocket，新增日志 REST 内容接口）

## 目标
- 移除 logger 侧所有 WebSocket 依赖与类；
- 保留日志列表/删除接口；
- 新增日志内容查询 REST 接口，供前端短轮询使用。

## 影响模块与文件
- 依赖：`flow-eda-logger/pom.xml` 移除 `spring-boot-starter-websocket`。
- 删除类：
  - `src/main/java/com/flow/eda/logger/ws/FlowLogWebsocket.java`
  - `src/main/java/com/flow/eda/logger/ws/LogContentWebsocket.java`
- 保留类：
  - `src/main/java/com/flow/eda/logger/logs/LogsController.java`
  - `src/main/java/com/flow/eda/logger/listener/*`、`writer/*`

## 实施步骤
1) 移除依赖
- 编辑 `flow-eda-logger/pom.xml`，删除：
  - `<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-websocket</artifactId></dependency>`

2) 删除 WebSocket 类
- 删除 `ws/` 目录下全部类。

3) 新增 REST：获取日志内容
- 在 `LogsController` 内新增：
  - `@GetMapping("/api/v1/feign/logs/content")`，参数 `path`（日志文件路径，来自 Logs.path），返回文本内容（首版全量）。
- 实现要点：
  - 从 `System.getProperty("user.dir") + path` 读取文本；
  - 不存在或异常时返回空字符串；
  - 控制最大返回大小（如 1MB）避免过大响应。

4) 编译与验证
- `mvn -pl flow-eda-logger -DskipTests=false clean package`
- 启动后验证：
  - `GET /api/v1/feign/logs` 返回列表；
  - `GET /api/v1/feign/logs/content?path=/logs/running/<flowId>/YYYY-MM-DD.log` 返回内容。

## TDD 测试策略
1) 先写测试再实现
- 为 `LogsController` 新增单元测试：
  - 列表接口：构造临时日志目录与文件，断言返回的 `Logs` 列表与大小/日期解析；
  - 内容接口：创建临时文件写入内容，调用 `/api/v1/feign/logs/content` 断言返回文本；
  - 异常路径：不存在文件、超大文件截断等；
- 避免真实 I/O 依赖：通过临时工作目录或封装读取逻辑并用 Mockito Stub。

2) 集成测试（可选）
- SpringBootTest 启动 logger 应用，Mock 掉文件读取服务，验证路由与序列化。

## 回归点
- 无任何 WebSocket 端点；
- 列表/删除接口可用；
- 日志内容接口可被 web 透传并被前端轮询显示。
