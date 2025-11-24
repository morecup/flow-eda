package com.flow.eda.server.flow.node.data;

import lombok.Data;

import java.util.Map;

@Data
public class NodeDataRunRequest {
    private String flowId;
    private String instanceId;
    private Map<String, Object> payload;
}
