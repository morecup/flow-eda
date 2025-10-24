package com.flow.eda.server.runtime;

import com.flow.eda.common.exception.FlowException;
import com.flow.eda.common.model.FlowData;
import com.flow.eda.server.runtime.node.Node;
import com.flow.eda.server.instance.FlowInstanceRepository;
import com.flow.eda.server.instance.FlowInstanceNodeDO;
import com.flow.eda.server.instance.FlowInstanceLogDO;
import com.flow.eda.server.instance.FlowInstanceDO;
import com.flow.eda.server.runtime.node.NodeTypeEnum;
import com.flow.eda.server.status.FlowStatusService;
import com.flow.eda.server.common.ApplicationContextUtil;
import com.flow.eda.server.utils.FlowLogs;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.flow.eda.common.utils.CollectionUtil.*;
import static com.flow.eda.server.runtime.FlowThreadPool.getThreadPool;
import static com.flow.eda.server.utils.FlowLogs.info;

/** 单条流程的执行者 */
public class FlowExecutor {
    /** 存储当前流程的完整流节点数据 */
    private final List<FlowData> flowData;
    // MQ 生产者按需获取，避免测试环境未加载上下文导致空指针
    /** 当前流程的id */
    private final String flowId;
    /** 流程状态服务 */
    private final FlowStatusService flowStatusService =
            ApplicationContextUtil.getBean(FlowStatusService.class);

    public FlowExecutor(List<FlowData> flowData, Object ignored) {
        this.flowData = flowData;
        this.flowId =
                Objects.requireNonNull(findFirst(flowData, n -> n.getFlowId() != null)).getFlowId();
    }

    /** 开始执行流程 */
    public void start(FlowData startNode) {
        this.run(startNode);
    }

    /** 执行当前节点 */
    private void run(FlowData currentNode) {
        String nodeName = currentNode.getNodeName();
        // 处理节点参数，设置flowId和nodeId
        if (currentNode.getParams() == null) {
            currentNode.setParams(new Document());
        }
        String input = currentNode.getParams().toJson();
        currentNode.getParams().append("flowId", flowId).append("nodeId", currentNode.getId());
        try {
            Node nodeInstance = getInstance(currentNode);
            // 处理阻塞节点
            if (nodeInstance instanceof FlowBlockNodePool.BlockNode) {
                FlowBlockNodePool.addBlockNode(flowId, (FlowBlockNodePool.BlockNode) nodeInstance);
            }
            // 更新节点运行状态
            if (nodeInstance.status() != null) {
                sendNodeStatus(currentNode, new Document("status", nodeInstance.status().name()));
                info(flowId, "start running [{}] node. input:{}", nodeName, input);
            } else {
                // 当前节点中断后，更新流程状态
                flowStatusService.removeRunningNode(flowId, currentNode.getId());
                if (flowStatusService.isFinished(flowId)) return;
            }
            // 执行节点
            nodeInstance.run(
                    (p) -> {
                        info(flowId, "run [{}] node finished. output:{}", nodeName, p.toJson());
                        try {
                            // 追加实例日志（best-effort）
                            FlowInstanceRepository repository = ApplicationContextUtil.getBean(FlowInstanceRepository.class);
                            FlowInstanceLogDO log = new FlowInstanceLogDO();
                            log.setInstanceId(flowId);
                            log.setNodeId(currentNode.getId());
                            log.setLevel("INFO");
                            log.setCategory("NODE");
                            log.setMessage("run [" + nodeName + "] node finished. output:" + p.toJson());
                            log.setLogTime(LocalDateTime.now());
                            log.setCreatedAt(LocalDateTime.now());
                            repository.saveLogs(java.util.Collections.singletonList(log));
                        } catch (Exception ignored) {}
                        runNext(currentNode, nodeInstance, p);
                    });
        } catch (Exception e) {
            String message;
            if (e.getMessage() != null) {
                message = FlowException.wrap(e, e.getMessage()).getMessage();
            } else {
                message = FlowException.wrap(e).getMessage();
            }
            Document status = new Document("status", Node.Status.FAILED.name());
            sendNodeStatus(currentNode, status.append("error", message));
            FlowLogs.error(flowId, "run [{}] node failed. error:{}", nodeName, message);
            try {
                FlowInstanceRepository repository = ApplicationContextUtil.getBean(FlowInstanceRepository.class);
                FlowInstanceLogDO log = new FlowInstanceLogDO();
                log.setInstanceId(flowId);
                log.setNodeId(currentNode.getId());
                log.setLevel("ERROR");
                log.setCategory("NODE");
                log.setMessage("run [" + nodeName + "] node failed. error:" + message);
                log.setLogTime(LocalDateTime.now());
                log.setCreatedAt(LocalDateTime.now());
                repository.saveLogs(java.util.Collections.singletonList(log));
            } catch (Exception ignored) {}
        }
    }

    /** 节点数据执行后回调，继续执行下一节点 */
    private void runNext(FlowData currentNode, Node nodeInstance, Document p) {
        // 所有完成的节点都保存输出信息，用于调试和监控
        if (Node.Status.FINISHED.equals(nodeInstance.status())) {
            Document output = new Document();
            output.putAll(p);
            output.remove("input");
            output.remove("payload");

            Document msg = new Document("status", Node.Status.FINISHED.name())
                    .append("output", output);
            sendNodeStatus(currentNode, msg);
        }
        // 多个下游节点，需要并行执行
        List<FlowData> nextNodes = getNextNode(currentNode);
        forEach(nextNodes, n -> getThreadPool(flowId).execute(() -> this.run(setInput(n, p))));
    }

    private List<FlowData> getNextNode(FlowData currentNode) {
        List<String> ids =
                filterMap(flowData, n -> currentNode.getId().equals(n.getFrom()), FlowData::getTo);
        if (isNotEmpty(ids)) {
            return filter(flowData, n -> ids.contains(n.getId()));
        }
        return null;
    }

    /** 获取当前节点的实例 */
    private Node getInstance(FlowData currentNode) {
        try {
            // 获取节点的构造函数，默认每个节点都有含参构造，获取不到时抛出异常
            Class<? extends Node> clazz = NodeTypeEnum.getClazzByNode(currentNode);
            // 初始化实例时会进行参数校验，校验不通过则会抛出异常
            Document params = new Document();
            params.putAll(currentNode.getParams());
            return clazz.getConstructor(Document.class).newInstance(params);
        } catch (Exception e) {
            throw FlowException.wrap(e);
        }
    }

    /** 设置input，上游节点的输出参数需要传递至下一节点 */
    private FlowData setInput(FlowData currentNode, Document input) {
        if (input != null) {
            // 自定义参数仅可传递至下一节点
            input.remove("payload");
            Document params = Optional.ofNullable(currentNode.getParams()).orElseGet(Document::new);
            params.putAll(input);
            currentNode.setParams(params);
        }
        return currentNode;
    }

    /** 推送节点状态 */
    private void sendNodeStatus(FlowData currentNode, Document msg) {
        String status = msg.getString("status");
        msg.append("nodeId", currentNode.getId());
        String instanceId = Optional.ofNullable(currentNode.getParams())
                .map(p -> p.getString("instanceId"))
                .orElse(flowId);

        // 若当前节点已运行完成,则检查NextNode
        if (Node.Status.FINISHED.name().equals(status)) {
            forEach(
                    getNextNode(currentNode),
                    node -> flowStatusService.addRunningNode(instanceId, node.getId()));
        }

        String flowStatus =
                ApplicationContextUtil.getBean(FlowStatusService.class)
                        .getFlowStatus(instanceId, msg);
        // MQ 已移除,状态计算仅用于内存管理

        // 保存节点状态到数据库
        saveNodeStatus(currentNode, msg, instanceId);

        // 检查流程是否已完成,如果完成则更新实例状态
        // 使用 instanceId 而不是 flowId，因为可能有多个实例同时运行同一个流程定义
        if (flowStatusService.isFinished(instanceId)) {
            updateInstanceStatus(instanceId, flowStatus);
        }
    }

    /** 保存节点状态到数据库 */
    private void saveNodeStatus(FlowData currentNode, Document msg, String instanceId) {
        try {
            FlowInstanceRepository repository = ApplicationContextUtil.getBean(FlowInstanceRepository.class);
            FlowInstanceNodeDO node = new FlowInstanceNodeDO();
            node.setInstanceId(instanceId);
            node.setNodeId(currentNode.getId());
            node.setNodeName(currentNode.getNodeName());
            node.setNodeType(currentNode.getType());
            node.setStatus(msg.getString("status"));

            // 设置输入输出
            if (currentNode.getParams() != null) {
                node.setInputJson(currentNode.getParams().toJson());
            }
            if (msg.containsKey("output")) {
                node.setOutputJson(msg.get("output", Document.class).toJson());
            }

            // 设置错误信息
            if (msg.containsKey("error")) {
                node.setErrorStack(msg.getString("error"));
            }

            // 设置时间
            if (Node.Status.RUNNING.name().equals(msg.getString("status"))) {
                node.setStartTime(LocalDateTime.now());
            } else if (Node.Status.FINISHED.name().equals(msg.getString("status"))
                    || Node.Status.FAILED.name().equals(msg.getString("status"))) {
                node.setEndTime(LocalDateTime.now());
            }

            node.setCreatedAt(LocalDateTime.now());
            node.setUpdatedAt(LocalDateTime.now());

            repository.saveNode(node);
        } catch (Exception e) {
            // Best-effort: 节点状态保存失败不影响流程执行
            FlowLogs.error(flowId, "save node status failed: {}", e.getMessage());
        }
    }

    /** 更新实例状态到数据库 */
    private void updateInstanceStatus(String instanceId, String status) {
        try {
            FlowInstanceRepository repository = ApplicationContextUtil.getBean(FlowInstanceRepository.class);
            FlowInstanceDO instance = repository.findInstanceById(instanceId)
                    .orElseThrow(() -> new RuntimeException("Instance not found: " + instanceId));
            instance.setStatus(status);
            instance.setEndTime(LocalDateTime.now());
            instance.setUpdatedAt(LocalDateTime.now());
            repository.saveInstance(instance);
            FlowLogs.info(flowId, "instance [{}] finished with status: {}", instanceId, status);
        } catch (Exception e) {
            // Best-effort: 实例状态更新失败不影响流程执行
            FlowLogs.error(flowId, "update instance status failed: {}", e.getMessage());
        }
    }
}
