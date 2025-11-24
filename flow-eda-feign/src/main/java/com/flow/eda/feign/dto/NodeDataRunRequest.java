package com.flow.eda.feign.dto;

import lombok.Data;

import java.util.Map;

/** 节点运行请求体 */
@Data
public class NodeDataRunRequest {
    private String flowId;
    private String instanceId;
    private Map<String, Object> payload;
}
