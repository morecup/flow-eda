# Flow-EDA Gateway 网关模块

## 概述

Flow-EDA Gateway 是基于 Spring Cloud Gateway 实现的微服务网关，提供统一的入口、路由转发、认证鉴权、跨域处理等功能。

## 功能特性

### 1. 路由转发
- **Web 服务**: `/flow-eda-web/**` → `flow-eda-web`
- **OAuth2 服务**: `/flow-eda-oauth2/**` → `flow-eda-oauth2`
- **Logger 服务**: `/flow-eda-logger/**` → `flow-eda-logger`
- **Runner 服务**: `/flow-eda-runner/**` → `flow-eda-runner`

### 2. 服务发现
- 集成 Nacos 服务发现
- 自动负载均衡（基于 Spring Cloud LoadBalancer）
- 动态路由更新

### 3. 认证鉴权
- 基于 Token 的认证机制
- 白名单配置（支持 Ant 路径匹配）
- 自动提取 Authorization Header

### 4. 跨域处理
- 全局 CORS 配置
- 支持所有来源、方法、头部
- 预检请求自动处理

### 5. 异常处理
- 统一异常响应格式
- 服务未找到提示
- 详细错误日志

### 6. 请求体缓存
- 解决 Gateway body 只能读取一次的问题
- 支持 POST/PUT/PATCH 请求

## 配置说明

### 端口配置
- 网关端口: `8080`
- 前端开发服务器: `3000`

### 白名单配置
在 `application.yaml` 中配置不需要认证的路径：

```yaml
ignore:
  whites:
    - /oauth/token
    - /oauth/check_token
    - /web/flow/data/page
    - /web/node/page
    - /actuator/**
```

### Nacos 配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:public}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
```

## 启动说明

### 1. 启动 Nacos
确保 Nacos 服务已启动并运行在 `localhost:8848`

### 2. 启动后端服务
按顺序启动以下服务：
- flow-eda-oauth2 (端口 8086)
- flow-eda-web (端口 8081)
- flow-eda-logger (端口 8082)
- flow-eda-runner (端口 8083)

### 3. 启动网关
```bash
cd flow-eda-gateway
mvn spring-boot:run
```

### 4. 启动前端
```bash
cd flow-eda-vue
npm run dev
```

## 请求流程

```
前端 (localhost:3000)
  ↓
Vite Proxy
  ↓
Gateway (localhost:8080)
  ↓
Nacos 服务发现
  ↓
后端微服务 (flow-eda-web/oauth2/logger/runner)
```

## 核心组件

### 过滤器 (Filter)

#### AuthFilter
- **优先级**: -200
- **功能**: 认证鉴权
- **逻辑**:
  - 检查白名单
  - 提取 Token
  - 验证 Token（可扩展）

#### CacheRequestBodyFilter
- **优先级**: HIGHEST_PRECEDENCE
- **功能**: 缓存请求体
- **适用**: POST/PUT/PATCH 请求

### 异常处理器 (Handler)

#### GatewayExceptionHandler
- **优先级**: -1
- **功能**: 统一异常处理
- **响应格式**:
```json
{
  "code": 500,
  "msg": "错误信息",
  "data": null
}
```

## 前端配置变更

### vite.config.js
```javascript
proxy: {
  "^/api/v1": {
    target: `http://localhost:8080`,  // 指向网关
    ws: true,
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/api\/v1/, '/web'),
  },
  "^/oauth": {
    target: `http://localhost:8080`,  // 指向网关
    changeOrigin: true,
  },
}
```

## 依赖说明

```xml
<!-- 核心依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

<!-- 服务发现 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>

<!-- 负载均衡 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-loadbalancer</artifactId>
</dependency>
```

## 扩展建议

### 1. 限流降级
可以集成 Sentinel 实现限流、降级功能：
```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

### 2. 日志追踪
可以集成 Sleuth 实现分布式链路追踪：
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

### 3. Token 验证增强
在 `AuthFilter` 中可以添加：
- 调用 OAuth2 服务验证 Token
- 解析 Token 获取用户信息
- 将用户信息传递给下游服务

## 故障排查

### 1. 服务未找到
- 检查 Nacos 中服务是否注册成功
- 检查服务名称是否正确
- 检查网络连接

### 2. 跨域问题
- 确认 CORS 配置是否生效
- 检查浏览器控制台错误信息
- 验证 OPTIONS 预检请求

### 3. 认证失败
- 检查白名单配置
- 验证 Token 格式
- 查看网关日志

## 注意事项

1. **端口冲突**: 确保 8080 端口未被占用
2. **服务启动顺序**: 先启动 Nacos，再启动各微服务，最后启动网关
3. **环境变量**: 可通过环境变量配置 Nacos 地址等参数
4. **生产环境**: 建议配置 HTTPS、限流、监控等安全措施

## 版本信息

- Spring Boot: 2.6.4
- Spring Cloud: 2021.0.1
- Spring Cloud Alibaba: 2021.0.1.0
- Java: 1.8
