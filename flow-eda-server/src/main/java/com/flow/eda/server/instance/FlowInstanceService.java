package com.flow.eda.server.instance;

import com.flow.eda.common.exception.ResourceNotFoundException;
import com.flow.eda.server.status.FlowStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FlowInstanceService {

    private static final Logger logger = LoggerFactory.getLogger(FlowInstanceService.class);

    private final FlowInstanceRepository repository;
    private final FlowStatusService flowStatusService;

    public FlowInstanceService(FlowInstanceRepository repository, FlowStatusService flowStatusService) {
        this.repository = repository;
        this.flowStatusService = flowStatusService;
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

    public List<FlowInstanceNodeDO> getInstanceNodes(String instanceId) {
        // 确保实例存在
        getInstance(instanceId);
        return repository.findNodesByInstanceId(instanceId);
    }

    public void saveNode(FlowInstanceNodeDO node) {
        // 确保实例存在
        getInstance(node.getInstanceId());

        try {
            repository.saveNode(node);
        } catch (Exception e) {
            // Best-effort: 节点状态保存失败记录日志，但不影响流程执行
            // 如果是死锁，切面会自动重试
            logger.warn("保存节点状态失败（实例: {}, 节点: {}）: {}",
                node.getInstanceId(), node.getNodeId(), e.getMessage());
        }
    }

    public void updateInstance(String instanceId, FlowInstanceUpdateRequest req) {
        FlowInstanceDO instance = getInstance(instanceId);
        if (req.getStatus() != null) {
            instance.setStatus(req.getStatus());
        }
        if (req.getEndTime() != null) {
            instance.setEndTime(req.getEndTime());
        }
        instance.setUpdatedAt(LocalDateTime.now());
        repository.saveInstance(instance);
    }

    public void stopInstance(String instanceId) {
        FlowInstanceDO instance = getInstance(instanceId);

        // 停止流程执行
        try {
            flowStatusService.stopInstance(instanceId);
        } catch (Exception e) {
            // Best-effort: 停止失败不影响数据库状态更新
            logger.warn("停止流程失败（实例: {}）: {}", instanceId, e.getMessage());
        }

        // 更新数据库状态
        instance.setStatus("STOPPED");
        instance.setEndTime(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());
        repository.saveInstance(instance);
    }
}


