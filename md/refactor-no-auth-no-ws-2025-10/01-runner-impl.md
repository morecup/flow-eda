# Runner 改造实施说明（移除 WebSocket 与 WS 节点）

## 目标
- 移除所有 WebSocket 依赖与代码；
- 删除 ws_server/ws_client 节点类型与实现；
- FlowExecutor 改为仅通过 FlowStatusService + MQ 维护/广播状态；
- FlowDataRuntime 停止/清理时不再推送 WS。

## 影响模块与文件
- 依赖：`flow-eda-runner/pom.xml` 移除 `spring-boot-starter-websocket`。
- 类删除：
  - `src/main/java/com/flow/eda/runner/status/FlowNodeWebsocket.java`
  - `src/main/java/com/flow/eda/runner/node/ws/**`（client、server、config、manager、handler 等）
- 枚举调整：`src/main/java/com/flow/eda/runner/node/NodeTypeEnum.java`
  - 移除常量：`WS_SERVER("ws_server", ...)`、`WS_CLIENT("ws_client", ...)`
- 运行时：
  - `src/main/java/com/flow/eda/runner/runtime/FlowExecutor.java`
  - `src/main/java/com/flow/eda/runner/runtime/FlowDataRuntime.java`
  - `src/main/java/com/flow/eda/runner/status/FlowStatusService.java`
  - `src/main/java/com/flow/eda/runner/status/FlowStatusMqProducer.java`

## 实施步骤
1) 移除依赖
- 编辑 `flow-eda-runner/pom.xml`，删除：
  - `<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-websocket</artifactId></dependency>`

2) 删除 WebSocket 相关类
- 删除 `FlowNodeWebsocket.java`；
- 删除 `node/ws/**` 下所有类（client/server/config/handler/manager 等）。

3) NodeTypeEnum 删除 WS 节点
- 从 `NodeTypeEnum` 中移除常量 `WS_SERVER`、`WS_CLIENT` 及相关 import。

4) FlowExecutor 状态推送改造
- 将原对 `flowNodeWebsocket.sendMessage(flowId, msg)` 的调用，替换为：
  - 计算/获取流程状态：`ApplicationContextUtil.getBean(FlowStatusService.class).getFlowStatus(flowId, msg)`；
  - 发送 MQ：`ApplicationContextUtil.getBean(FlowStatusMqProducer.class).sendFlowStatus(flowId, flowStatus)`。
- 当节点完成（FINISHED）或失败（FAILED）时按上述流程广播；去除所有 WS 发送逻辑与 Session 同步块。

5) FlowDataRuntime 停止/清理改造
- 删除对 `ws.sendMessage(...)` 的调用；
- 停止/清理时，仅：
  - 关闭线程池/阻塞节点；
  - 基于 `flowStatusService.getRunningNodes(flowId)` 判断并通过 MQ 发送最终状态（`FINISHED`）。

6) 编译校验
- `mvn -pl flow-eda-runner -DskipTests=false clean package`
- 处理编译期因删除类导致的引用残留（按提示清理 import 或代码）。

## TDD 测试策略
1) 先写测试再改造（红 → 绿 → 重构）
- 状态计算：为 `FlowStatusService` 增加单元测试，覆盖 `startRun/add/remove/isFinished/getFlowStatus/clear` 分支；
- 执行器行为：为 `FlowExecutor` 编写测试桩，模拟 Node 执行完成/失败，断言对 `FlowStatusService` 与 `FlowStatusMqProducer` 的交互（使用 Mockito）；
- 运行时：为 `FlowDataRuntime` 的 `stopFlowData/clearFlowData` 编写测试，断言线程池关闭与最终状态发送；

2) 删除 WS 后的回归测试
- 确认无 `@ServerEndpoint` Bean；
- NodeTypeEnum 不包含 WS 常量；
- 运行流程的集成测试（可用 SpringBootTest + Testcontainers/RabbitMQ 或 Mock RabbitTemplate）验证 MQ 发送。

## 回归点
- 启动 runner 后无 WebSocket 端点；
- 运行流程时，通过 MQ 能看到 `flow.status.updated` 消息；
- web 侧轮询能正确获取流程 `RUNNING/FINISHED/FAILED`；
- 无 ws_* 节点可用（前端节点类型也不应出现）。
