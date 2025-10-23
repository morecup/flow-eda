package com.flow.eda.runner.controller;

import com.flow.eda.runner.status.FlowStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 实例控制器 - 负责处理实例级别的操作
 */
@RestController
@RequestMapping("/api/instances")
public class InstanceController {

    private final FlowStatusService flowStatusService;

    public InstanceController(FlowStatusService flowStatusService) {
        this.flowStatusService = flowStatusService;
    }

    /**
     * 停止指定实例的执行
     *
     * @param instanceId 实例ID
     * @return 响应
     */
    @PostMapping("/{instanceId}/stop")
    public ResponseEntity<Void> stopInstance(@PathVariable String instanceId) {
        flowStatusService.stopInstance(instanceId);
        return ResponseEntity.ok().build();
    }
}
