package com.flow.eda.runner.node;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 流程实例节点状态 Feign 客户端
 */
@FeignClient(name = "flow-eda-web")
public interface InstanceNodeClient {
    @PostMapping("/api/flow/instances/{id}/nodes")
    void saveNode(@PathVariable("id") String instanceId, @RequestBody InstanceNodeRecord node);
}
