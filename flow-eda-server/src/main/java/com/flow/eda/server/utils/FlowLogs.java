package com.flow.eda.server.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 流程运行日志工具（简化为控制台输出；实例日志由 Web 入库）
 */
@Slf4j
public class FlowLogs {
    public static void info(String flowId, String msg, Object... args) {
        log.info("[flowId:{}] " + msg, flowId, args);
    }

    public static void error(String flowId, String msg, Object... args) {
        log.error("[flowId:{}] " + msg, flowId, args);
    }
}
