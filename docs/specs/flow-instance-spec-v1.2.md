# 流程实例化需求规格 v1.2

## 1. 背景与目标

- 现有系统以 `flowId` 为核心，仅能单实例运行且日志维度单一。
- 新需求要求支持 **同一流程定义多实例并行运行**，并提供实例视角的运行监控与干预能力。
- 同步下线 `flow-eda-logger` 模块及所有基于 `flowId` 的运行/日志逻辑，建立以 `instanceId` 为核心的统一数据与接口体系。

## 2. 范围

1. **运行能力**：实例启动、状态查询、终止、重试，全部采用 REST 轮询方案。
2. **数据持久化**：节点输入/输出、节点日志、实例日志、实例元数据落库；移除旧日志服务与文件写入依赖。
3. **前端改造**：编辑器界面支持实例列表、实例切换、节点状态轮询、终止/重试操作。
4. **接口改造**：Web 服务提供实例与日志 API；旧 `flowId` 启动及日志接口下线。
5. **测试与部署**：补充自动化测试、数据库脚本与部署指引。

## 3. 术语

| 名称 | 说明 |
| ---- | ---- |
| 流程定义（Flow） | 编辑器中保存的流程拓扑与节点配置。 |
| 流程实例（Instance） | 基于流程定义运行得到的单次执行上下文，唯一标识为 `instanceId`。 |
| 节点实例（Instance Node） | 实例运行过程中某个节点的一次执行快照（含输入、输出、状态、耗时）。 |
| 实例日志（Instance Log） | 按实例维度产生的运行日志、告警、节点日志等结构化记录。 |

## 4. 功能需求

### 4.1 实例运行

- **启动实例**：提交流程定义 ID，生成 `instanceId` 后立即异步运行，可并行多个实例。
- **状态更新**：Runner 实时更新实例与节点状态，Web 端提供轮询接口查询。
- **终止实例**：在 RUNNING 状态下手动终止，停止所有节点执行并记录终止原因。
- **重试实例**：在 FAILED/TERMINATED 状态下重新运行，从失败节点继续执行，并继承最后一次输入快照。

### 4.2 数据查询

- **实例列表**：按流程定义查询所有历史与运行中实例，包含状态、开始/结束时间、触发人等信息。
- **实例详情**：展示当前状态、运行中的节点、历史节点列表、失败原因。
- **节点快照**：查询指定实例所有节点的输入、输出、状态、耗时、异常栈。
- **日志查询**：实例及节点日志支持分页、时间范围、节点过滤。

### 4.3 前端交互

- “开始流程”按钮改为“启动实例”，成功后反馈 `instanceId`。
- 在编辑器右侧/弹窗展示实例列表，可选中实例并触发定时轮询（默认 3~5 秒）。
- 节点面板高亮当前实例运行状态（运行中/已完成/失败/终止）。
- 在实例详情中提供终止、重试按钮及日志查看入口。
- 全站禁用 WebSocket，轮询接口用于刷新状态与日志。

## 5. 非功能需求

- **可靠性**：实例状态切换、终止、重试须具备最终一致性，错误需有日志记录。
- **性能**：轮询接口须支持 1 秒内响应，单流程 10 实例并行时无明显性能退化。
- **安全性**：当前阶段无角色控制，后续权限能力需容易接入。
- **可维护性**：所有新增逻辑保持单元测试覆盖，Runner/Web/前端均需构建通过。

## 6. 数据模型

### 6.1 新增表

1. `flow_instance`
   - 字段：`id (PK)`, `flow_id`, `status`, `start_time`, `end_time`, `trigger_user`, `terminate_reason`, `retry_count`, `created_at`, `updated_at`。
2. `flow_instance_node`
   - 字段：`id (PK)`, `instance_id (FK)`, `node_id`, `node_name`, `node_type`, `status`, `start_time`, `end_time`, `duration_ms`, `input_json`, `output_json`, `error_stack`, `created_at`, `updated_at`。
3. `flow_instance_log`
   - 字段：`id (PK)`, `instance_id (FK)`, `node_id`, `level`, `category`, `message`, `payload_json`, `log_time`, `created_at`。

### 6.2 迁移与清理

- 编写 SQL 脚本创建上述数据表及必要索引。
- 清理 `flow-eda-logger` 相关表、配置与数据写入逻辑。
- Runner 与 Web 相关实体、Mapper、DAO 更新至实例维度。

## 7. 系统设计调整

### 7.1 Runner

- `FlowExecutor`：初始化时生成 `instanceId`，并在运行流程生命周期内传递。
- `FlowStatusService`、`FlowStatusClient`：内部状态映射改为 `Map<instanceId, ...>`，支持并行实例。
- 节点执行：在每次节点运行前后记录输入、输出、状态和日志，并写入数据库。
- 终止/重试：暴露 `stopInstance(instanceId)`、`retryInstance(instanceId)` 接口，与 `FlowBlockNodePool`、`ValveNodeManager` 等资源管理联动。

### 7.2 Web 服务

- 新增 `FlowInstanceController` 提供 REST API：
  - `POST /api/flow/instances`
  - `GET /api/flow/instances?flowId=`
  - `GET /api/flow/instances/{instanceId}`
  - `GET /api/flow/instances/{instanceId}/nodes`
  - `GET /api/flow/instances/{instanceId}/logs`
  - `POST /api/flow/instances/{instanceId}/terminate`
  - `POST /api/flow/instances/{instanceId}/retry`
- 数据响应需包含实例状态、节点状态列表、分页日志数据。
- 删除旧 `flowId` 启动接口、WebSocket 推送、Logger 相关依赖。

### 7.3 前端（flow-eda-vue）

- `Flow` 页面新增实例列表面板、轮询逻辑、终止/重试操作按钮。
- 编辑器根据轮询结果动态更新节点样式、状态标签、节点详情弹窗。
- 日志页面改为实例维度，支持查看单实例/单节点日志。
- 删除 WebSocket 客户端代码，新增轮询定时器与取消机制。

## 8. 验收标准（Given / When / Then）

1. **实例并行运行**
   - Given 同一流程定义被多次启动
   - When 在短时间内触发三次实例
   - Then 系统返回三个不同 `instanceId` 并全部正确运行，互不干扰。
2. **实例状态查询**
   - Given 某实例正在运行
   - When 轮询 `GET /api/flow/instances/{id}`
   - Then 返回状态为 RUNNING 且列出当前运行节点、历史节点、失败原因（若有）。
3. **节点输入输出记录**
   - Given 实例节点执行完成
   - When 查询 `flow_instance_node`
   - Then 记录包含节点输入、输出、执行状态、耗时及时间戳。
4. **实例日志查询**
   - Given 实例产生节点日志
   - When 调用日志接口分页查询
   - Then 返回日志条目携带 `instanceId`、`nodeId`、级别、内容、时间。
5. **实例终止**
   - Given 实例状态 RUNNING
   - When 调用终止接口
   - Then 节点全部停止、实例状态变为 TERMINATED，并在日志中记录终止原因。
6. **实例重试**
   - Given 实例状态 FAILED
   - When 调用重试接口
   - Then 实例状态重新变为 RUNNING，并从失败节点继续执行，日志记录重试动作。
7. **旧功能清理**
   - Given 请求旧 `flowId` 启动或日志接口
   - When 访问该接口
   - Then 返回 404 或明确提示下线，代码库中无 `flow-eda-logger` 模块引用。

## 9. 测试计划

- Runner 单元测试：覆盖实例启动、终止、重试、节点记录、异常处理。
- Web API 单元/集成测试：验证新 REST 接口响应结构、分页、过滤。
- 前端端到端测试：实例启动、列表轮询、节点状态更新、终止/重试流程。
- 性能测试：在本地模拟多实例并行，确保轮询接口响应 < 1s。

## 10. 部署与回滚

- 数据库脚本需先执行，确保所有表、索引存在。
- 部署步骤：停止旧服务 → 执行 SQL → 部署新 Runner/Web → 部署前端 → 验证实例功能。
- 回滚策略：
  1. 停止新服务，回滚代码至旧版本。
  2. 根据备份表或 DDL 脚本恢复旧数据结构。
  3. 清理新生成的实例数据表（按需保留）。

## 11. 风险与缓解

- **数据库变更风险**：提前在测试环境演练，发布前备份关键表。
- **实例终止/重试一致性**：添加状态机约束与事务保障，并记录错误日志。
- **轮询压力**：设置合理轮询间隔、接口分页与限流策略。
- **旧功能依赖**：在开发初期定位所有 `flow-eda-logger` 引用，确保一次性移除。

## 12. 未决问题

- 无（如后续新增边界条件，再通过 ADR 或补充版本更新）。
