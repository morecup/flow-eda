package com.flow.eda.gateway.filter;

import com.flow.eda.gateway.config.IgnoreWhiteProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 认证过滤器
 *
 * @author flow-eda
 */
@Slf4j
// @Component  // 临时禁用认证过滤器，开发阶段使用
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private IgnoreWhiteProperties ignoreWhite;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 跳过白名单路径
        if (isWhitePath(path)) {
            return chain.filter(exchange);
        }

        // 获取 token
        String token = getToken(request);
        if (token == null || token.isEmpty()) {
            log.warn("请求路径: {} 缺少认证令牌", path);
            return unauthorizedResponse(exchange, "令牌不能为空");
        }

        // 这里可以添加 token 验证逻辑
        // 例如：调用 OAuth2 服务验证 token

        return chain.filter(exchange);
    }

    /**
     * 判断是否为白名单路径
     */
    private boolean isWhitePath(String path) {
        for (String pattern : ignoreWhite.getWhites()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取请求 token
     */
    private String getToken(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String token = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return token;
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");

        String body = String.format("{\"code\":401,\"msg\":\"%s\"}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
