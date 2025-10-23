package com.flow.eda.web.flow.instance;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/flow/instances")
public class FlowInstanceController {

    private final FlowInstanceService service;

    public FlowInstanceController(FlowInstanceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> start(@RequestBody Map<String, Object> req) {
        String flowId = (String) req.get("flowId");
        String triggerUser = (String) req.getOrDefault("triggerUser", "");
        String instanceId = service.startInstance(flowId, triggerUser);
        Map<String, Object> body = new HashMap<>();
        body.put("instanceId", instanceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable("id") String id) {
        FlowInstanceDO instance = service.getInstance(id);
        Map<String, Object> body = new HashMap<>();
        body.put("instanceId", instance.getId());
        body.put("status", instance.getStatus());
        return body;
    }

    @GetMapping("/{id}/logs")
    public List<FlowInstanceLogDO> logs(@PathVariable("id") String id) {
        return service.getInstanceLogs(id);
    }

    @PostMapping("/{id}/logs")
    public ResponseEntity<Void> appendLogs(@PathVariable("id") String id, @RequestBody List<FlowInstanceLogDO> logs) {
        service.appendLogs(id, logs);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/nodes")
    public List<FlowInstanceNodeDO> getNodes(@PathVariable("id") String id) {
        return service.getInstanceNodes(id);
    }

    @PostMapping("/{id}/nodes")
    public ResponseEntity<Void> saveNode(@PathVariable("id") String id, @RequestBody FlowInstanceNodeDO node) {
        node.setInstanceId(id);
        service.saveNode(node);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateInstance(@PathVariable("id") String id, @RequestBody Map<String, Object> req) {
        service.updateInstance(id, req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Void> stopInstance(@PathVariable("id") String id) {
        service.stopInstance(id);
        return ResponseEntity.ok().build();
    }
}


