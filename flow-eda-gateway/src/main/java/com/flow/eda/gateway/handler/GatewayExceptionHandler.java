package com.flow.eda.gateway.handler;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关统一异常处理
 *
 * @author flow-eda
 */
@Slf4j
@Order(-1)
@Configuration
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        String msg;
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        if (ex instanceof NotFoundException) {
            msg = "服务未找到";
            httpStatus = HttpStatus.NOT_FOUND;
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            msg = responseStatusException.getMessage();
            httpStatus = responseStatusException.getStatus();
        } else {
            msg = "内部服务器错误";
        }

        log.error("[网关异常处理] 请求路径: {}, 异常信息: {}", exchange.getRequest().getPath(), ex.getMessage(), ex);

        return writeResponse(response, httpStatus, msg);
    }

    /**
     * 写入响应
     */
    private Mono<Void> writeResponse(ServerHttpResponse response, HttpStatus httpStatus, String message) {
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> result = new HashMap<>(3);
        result.put("code", httpStatus.value());
        result.put("msg", message);
        result.put("data", null);

        DataBufferFactory bufferFactory = response.bufferFactory();
        return response.writeWith(Mono.just(bufferFactory.wrap(JSON.toJSONBytes(result))));
    }
}
