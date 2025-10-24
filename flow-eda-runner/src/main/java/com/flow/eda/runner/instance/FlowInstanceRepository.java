package com.flow.eda.runner.instance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

/**
 * 流程实例数据访问仓储
 */
@Repository
public class FlowInstanceRepository {

    private final FlowInstanceMapper mapper;

    public FlowInstanceRepository(FlowInstanceMapper mapper) {
        this.mapper = mapper;
    }

    public void saveInstance(FlowInstanceDO instance) {
        LocalDateTime now = Optional.ofNullable(instance.getUpdatedAt()).orElseGet(LocalDateTime::now);
        instance.setUpdatedAt(now);
        if (instance.getCreatedAt() == null) {
            instance.setCreatedAt(now);
        }
        if (instance.getRetryCount() == null) {
            instance.setRetryCount(0);
        }
        if (mapper.updateInstance(instance) == 0) {
            mapper.insertInstance(instance);
        }
    }

    public Optional<FlowInstanceDO> findInstanceById(String instanceId) {
        return Optional.ofNullable(mapper.selectInstanceById(instanceId));
    }

    public List<FlowInstanceDO> findInstancesByFlowId(String flowId) {
        return mapper.selectInstancesByFlowId(flowId);
    }

    public void saveNodes(List<FlowInstanceNodeDO> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        nodes.forEach(node -> {
            node.setUpdatedAt(now);
            if (node.getCreatedAt() == null) {
                node.setCreatedAt(now);
            }
        });
        mapper.insertNodes(nodes);
    }

    public void saveNode(FlowInstanceNodeDO node) {
        LocalDateTime now = LocalDateTime.now();
        node.setUpdatedAt(now);
        if (node.getCreatedAt() == null) {
            node.setCreatedAt(now);
        }
        mapper.insertOrUpdateNode(node);
    }

    public List<FlowInstanceNodeDO> findNodesByInstanceId(String instanceId) {
        return mapper.selectNodesByInstanceId(instanceId);
    }

    public void saveLogs(List<FlowInstanceLogDO> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }
        List<FlowInstanceLogDO> prepared = logs.stream()
                .map(log -> {
                    FlowInstanceLogDO copy = new FlowInstanceLogDO();
                    copy.setInstanceId(log.getInstanceId());
                    copy.setNodeId(log.getNodeId());
                    copy.setLevel(log.getLevel());
                    copy.setCategory(log.getCategory());
                    copy.setMessage(log.getMessage());
                    copy.setPayloadJson(log.getPayloadJson());
                    copy.setLogTime(Optional.ofNullable(log.getLogTime()).orElseGet(LocalDateTime::now));
                    copy.setCreatedAt(Optional.ofNullable(log.getCreatedAt()).orElseGet(LocalDateTime::now));
                    return copy;
                })
                .collect(Collectors.toList());
        mapper.insertLogs(prepared);
    }

    public List<FlowInstanceLogDO> findLogsByInstanceId(String instanceId) {
        return mapper.selectLogsByInstanceId(instanceId);
    }

    public void deleteInstancesByFlowId(String flowId) {
        List<FlowInstanceDO> instances = mapper.selectInstancesByFlowId(flowId);
        if (instances.isEmpty()) {
            return;
        }
        List<String> instanceIds = instances.stream().map(FlowInstanceDO::getId).collect(Collectors.toList());
        mapper.deleteLogsByInstanceIds(instanceIds);
        mapper.deleteNodesByInstanceIds(instanceIds);
        mapper.deleteInstancesByFlowId(flowId);
    }
}
