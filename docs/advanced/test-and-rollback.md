# 实例化功能测试计划与部署回滚方案

## 测试范围

- 后端 Runner：`FlowExecutor` 使用 `instanceId` 上报状态（单测覆盖 running/finished）
- 后端 Web：`FlowInstanceController` 创建/查询实例接口（单测覆盖 201/200 与 404）
- 前端 Vue：实例化 API start/get/轮询（最小脚本校验）与编辑器运行按钮联动显示

## 测试用例要点

- 创建实例成功：返回非空 `instanceId`
- 查询实例：存在返回 200，包含 `instanceId/status`；不存在返回 404
- 轮询逻辑：在 maxAttempts 内收到 FINISHED/FAILED 即结束；超时抛出异常
- 运行按钮：点击后状态切换为 RUNNING，收到 FINISHED 后重置轮询

## 回滚方案

1. 如 Web/Runner 任一模块合并后出现异常：
   - 通过 GitHub 回滚 PR：`Revert` 最近合并的 PR（#8/#9）并重新触发构建
   - 保留已创建的表结构，不执行 destructive 变更
2. 如前端改造异常：
   - 回滚前端 PR（#9），保留 API 导航与代理配置
3. 灰度与验证：
   - 在 dev 环境以指定 `flowId` 创建实例，观察日志与状态轮询一致性
   - 压测轮询接口（限流阈值与缓存）

## 风险与缓解

- 风险：轮询频率过高导致后端压力增大
  - 缓解：前端 interval ≥ 1s，后端缓存状态接口响应
- 风险：实例状态一致性
  - 缓解：以 `instanceId` 为唯一键，后端状态来源统一


