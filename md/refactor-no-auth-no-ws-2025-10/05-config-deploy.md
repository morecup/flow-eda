# 配置与部署实施说明（移除 WS 代理，清理 OAuth2 配置）

## 目标
- 清理各服务 application.yaml 中 OAuth2 配置；
- 删除 Vite 与网关中的 WS 代理/路由；
- 部署直接替换启动。

## 影响范围
- 配置：`flow-eda-web`、`flow-eda-runner`、`flow-eda-logger` 的 `application.yaml`
- 前端：`flow-eda-vue/vite.config.js`
- 网关（若有）：删除 `/ws/*` 路由转发

## 实施步骤
1) 配置清理
- 删除 `flow.oauth2.*` 相关配置（若存在）；
- 保留其它必要配置（数据源、rabbitmq、nacos 等）。

2) 代理/路由清理
- `vite.config.js` 删除 `^/ws/flow` 与 `^/ws/logs` 代理。
- 网关配置中删除对应 WS 路由转发至 runner/logger 的配置。

3) 构建与发布
- 后端：`mvn -DskipTests=false clean package`
- 前端：`npm run build`
- 替换部署包并启动（内部系统，无灰度）。

## 回归点
- 配置中无 oauth2 相关项；
- 无任何 `/ws/*` 路由；
- 系统运行流程与日志功能正常。
