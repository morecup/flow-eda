package com.flow.eda.runner.instance;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * 流程实例 Controller
 *
 * 职责：
 * - 启动流程实例
 * - 查询实例状态
 * - 查询节点执行详情
 * - 查询执行日志
 * - 停止运行中的实例
 */
@RestController
@RequestMapping("/api/instances")
public class InstanceController {

    private final FlowInstanceService service;

    public InstanceController(FlowInstanceService service) {
        this.service = service;
    }

    /**
     * 启动流程实例
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> start(@RequestBody Map<String, Object> req) {
        String flowId = (String) req.get("flowId");
        String triggerUser = (String) req.getOrDefault("triggerUser", "");
        String instanceId = service.startInstance(flowId, triggerUser);
        Map<String, Object> body = new HashMap<>();
        body.put("instanceId", instanceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /**
     * 查询实例详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable("id") String id) {
        FlowInstanceDO instance = service.getInstance(id);
        Map<String, Object> body = new HashMap<>();
        body.put("instanceId", instance.getId());
        body.put("status", instance.getStatus());
        return body;
    }

    /**
     * 查询实例日志
     */
    @GetMapping("/{id}/logs")
    public List<FlowInstanceLogDO> logs(@PathVariable("id") String id) {
        return service.getInstanceLogs(id);
    }

    /**
     * 查询实例节点状态
     */
    @GetMapping("/{id}/nodes")
    public List<FlowInstanceNodeDO> getNodes(@PathVariable("id") String id) {
        return service.getInstanceNodes(id);
    }

    /**
     * 停止实例
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Void> stopInstance(@PathVariable("id") String id) {
        service.stopInstance(id);
        return ResponseEntity.ok().build();
    }
}
