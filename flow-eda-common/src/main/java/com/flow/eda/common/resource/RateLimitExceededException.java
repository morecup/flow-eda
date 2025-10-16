package com.flow.eda.common.resource;

/** 占位异常（Oauth2移除后不再使用） */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String msg) { super(msg); }
}
