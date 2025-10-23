package com.flow.eda.web.flow.instance;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Runner 服务的 Feign 客户端
 */
@FeignClient(name = "flow-eda-runner")
public interface RunnerClient {

    /**
     * 调用 Runner 停止实例执行
     *
     * @param instanceId 实例ID
     */
    @PostMapping("/api/instances/{instanceId}/stop")
    void stopInstance(@PathVariable("instanceId") String instanceId);
}
