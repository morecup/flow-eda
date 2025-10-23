# Flow EDA

## 项目概览

Flow EDA 是一套事件驱动的可视化流程编排平台，用户可以通过拖拽节点、连线和配置参数的方式构建自动化业务流。当前主分支基于 Spring Cloud + Nacos 的服务注册治理方案，移除了历史上的 OAuth2 模块与 WebSocket 推送，统一采用 HTTP/RabbitMQ 组合实现流程状态轮询与日志分发，以便在内网或本地环境更加轻量地部署与调试。

在线文档与功能说明请访问：<https://linxfeng.github.io/flow-eda>

## 系统架构

后端采用微服务分层，服务间通过 HTTP 与 RabbitMQ 交互：

- **flow-eda-common**：公共依赖及通用配置（异常处理、工具类、Feign 基础配置等）。
- **flow-eda-runner**：流程运行引擎，负责调度节点、维护流程上下文、输出实时状态及运行日志。
- **flow-eda-web**：流程与节点管理服务，提供流程建模、节点类型元数据、运行监控、日志查询等接口。
// flow-eda-logger 已下线：日志改为实例维度入库查询，由 flow-eda-web 提供 API。
- **flow-eda-gateway**：统一网关，聚合对外 REST 请求，并提供基础的请求包装与异常处理。
- **flow-eda-vue**：Vue3 + Vite 构建的管理前端，负责流程编辑器、运行监控与日志可视化。

RabbitMQ 用于在 Runner 与 Logger/Web 之间传递运行日志；MySQL 存储流程、节点、运行记录等业务数据；Nacos 作为服务注册中心，支持多服务协同部署。

## 核心能力

- 可视化流程建模：支持任意拖拽、连线、导入/导出，配置自定义参数与模板。
- 丰富节点库：内置 HTTP、数据库、MQTT、定时、条件判断、子流程等节点，并支持按需扩展。
- 并行执行引擎：自动识别多起点，支持阻塞与非阻塞节点混合，保证数据隔离。
- 运行状态追踪：通过 REST 轮询获取流程状态、节点执行结果和异常信息。
- 日志中心：运行日志异步写入，可在前端进行实时/历史检索与追踪。
- 无登录版体验：默认关闭鉴权模块，开箱即用；如需企业级权限控制，可基于现有模块进行二次开发。

## 技术栈

| 类别     | 组件与版本示例                             |
| -------- | ------------------------------------------ |
| 后端框架 | Spring Boot 2.6.x、Spring Cloud 2021.0.x   |
| 注册配置 | Nacos 2.x                                  |
| 消息队列 | RabbitMQ 3.x                               |
| 数据访问 | MyBatis、PageHelper、HikariCP              |
| 数据存储 | MySQL 8.x                                  |
| 前端框架 | Vue 3、Vite、Pinia、Element Plus           |
| 构建工具 | Maven 3.6+、Node.js 16+                    |

## 环境准备

1. 安装 JDK 8、Maven 3.6+、Node.js 16+。
2. 准备 MySQL、RabbitMQ、Nacos 服务（可使用本仓库提供的 `docker-compose.yml` 快速拉起 RabbitMQ 与 Nacos）。
3. 在 MySQL 中执行 `flow-eda-common/sql` 目录的脚本：
   - `nacos_config.sql`（供 Nacos 初始化配置使用）。
   - `flow_eda.sql`（初始化业务库）。

> 注意：旧版所需的 `flow_eda_oauth2.sql` 已不再使用。

## 后端启动指南

1. 在项目根目录执行 `mvn clean package -DskipTests` 生成各模块可执行 JAR。
2. 按顺序启动基础设施与服务（可在 IDE 内逐个运行，也可通过命令行执行）：
   ```bash
   # 启动 Runner（流程调度引擎）
   java -jar flow-eda-runner/target/flow-eda-runner-0.0.1-SNAPSHOT.jar

   # 启动 Web（流程管理服务）
   java -jar flow-eda-web/target/flow-eda-web-0.0.1-SNAPSHOT.jar

   # 可选：启动 Gateway 统一出口
   java -jar flow-eda-gateway/target/flow-eda-gateway-0.0.1-SNAPSHOT.jar
   ```
3. 在 `application.yaml` 中根据自身环境调整 MySQL、RabbitMQ、Nacos 地址及凭据，避免使用示例中的测试密码。

## 前端启动指南

```bash
cd flow-eda-vue
npm install
npm run dev
```

默认开发端口为 `5173`，可在 `vite.config.js` 中自定义代理。由于取消登录流程，直接访问前端页面即可体验全部功能；如需面向外部用户，建议在网关侧增加鉴权逻辑。

## 常见问题

- **服务注册失败**：确认 Nacos 服务已启动，`NACOS_SERVER_ADDR` 环境变量配置正确。
- **日志无法刷新**：确保 RabbitMQ 正常运行，`flow.log` 交换机与路由键与配置一致。
- **流程执行异常**：查看前端运行日志或 `logs` 目录下 Logger 落盘文件，定位节点输入输出。

更多部署、节点扩展与二次开发指南，请参考 `docs` 目录或在线文档。

## 许可证

本项目遵循 MIT License，详见根目录 `LICENSE` 文件。
