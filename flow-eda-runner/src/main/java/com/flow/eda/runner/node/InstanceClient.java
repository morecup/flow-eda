package com.flow.eda.runner.node;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 流程实例 Feign 客户端
 */
@FeignClient(name = "flow-eda-web")
public interface InstanceClient {
    @PutMapping("/api/flow/instances/{id}")
    void updateInstance(@PathVariable("id") String instanceId, @RequestBody InstanceUpdateRequest request);
}
