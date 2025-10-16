package com.flow.eda.common.resource;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

/** 关闭微服务之间的 OAuth2 认证拦截 */
@Configuration
public class FeignOauth2RequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // no-op
    }
}
