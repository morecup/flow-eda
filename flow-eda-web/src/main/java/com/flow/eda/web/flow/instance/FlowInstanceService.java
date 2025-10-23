package com.flow.eda.web.flow.instance;

import com.flow.eda.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FlowInstanceService {

    private final FlowInstanceRepository repository;

    public FlowInstanceService(FlowInstanceRepository repository) {
        this.repository = repository;
    }

    public String startInstance(String flowId, String triggerUser) {
        String instanceId = UUID.randomUUID().toString();
        FlowInstanceDO instance = new FlowInstanceDO();
        instance.setId(instanceId);
        instance.setFlowId(flowId);
        instance.setTriggerUser(triggerUser);
        instance.setStatus("RUNNING");
        instance.setStartTime(LocalDateTime.now());
        instance.setCreatedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());
        repository.saveInstance(instance);
        return instanceId;
    }

    public FlowInstanceDO getInstance(String instanceId) {
        return repository.findInstanceById(instanceId)
                .orElseThrow(() -> new ResourceNotFoundException("instance not found: " + instanceId));
    }

    public List<FlowInstanceLogDO> getInstanceLogs(String instanceId) {
        // 确保实例存在
        getInstance(instanceId);
        return repository.findLogsByInstanceId(instanceId);
    }

    public void appendLogs(String instanceId, List<FlowInstanceLogDO> logs) {
        // 确保实例存在
        getInstance(instanceId);
        if (logs == null || logs.isEmpty()) return;
        logs.forEach(l -> l.setInstanceId(instanceId));
        repository.saveLogs(logs);
    }
}


