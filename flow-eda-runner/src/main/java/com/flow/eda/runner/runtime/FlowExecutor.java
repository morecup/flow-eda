package com.flow.eda.runner.runtime;

import com.flow.eda.common.exception.FlowException;
import com.flow.eda.common.model.FlowData;
import com.flow.eda.runner.node.Node;
import com.flow.eda.runner.logs.InstanceLogClient;
import com.flow.eda.runner.logs.InstanceLogRecord;
import com.flow.eda.runner.node.NodeTypeEnum;
import com.flow.eda.runner.status.FlowStatusService;
import com.flow.eda.runner.utils.ApplicationContextUtil;
import com.flow.eda.runner.utils.FlowLogs;
import org.bson.Document;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.flow.eda.common.utils.CollectionUtil.*;
import static com.flow.eda.runner.runtime.FlowThreadPool.getThreadPool;
import static com.flow.eda.runner.utils.FlowLogs.info;

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
                            InstanceLogClient client = ApplicationContextUtil.getBean(InstanceLogClient.class);
                            InstanceLogRecord rec = new InstanceLogRecord();
                            rec.setNodeId(currentNode.getId());
                            rec.setLevel("INFO");
                            rec.setCategory("NODE");
                            rec.setMessage("run [" + nodeName + "] node finished. output:" + p.toJson());
                            client.append(flowId, java.util.Collections.singletonList(rec));
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
                InstanceLogClient client = ApplicationContextUtil.getBean(InstanceLogClient.class);
                InstanceLogRecord rec = new InstanceLogRecord();
                rec.setNodeId(currentNode.getId());
                rec.setLevel("ERROR");
                rec.setCategory("NODE");
                rec.setMessage("run [" + nodeName + "] node failed. error:" + message);
                client.append(flowId, java.util.Collections.singletonList(rec));
            } catch (Exception ignored) {}
        }
    }

    /** 节点数据执行后回调，继续执行下一节点 */
    private void runNext(FlowData currentNode, Node nodeInstance, Document p) {
        // 推送节点消息
        if (NodeTypeEnum.OUTPUT.getType().equals(currentNode.getType())) {
            sendOutput(currentNode, p);
        } else if (Node.Status.FINISHED.equals(nodeInstance.status())) {
            sendNodeStatus(currentNode, new Document("status", Node.Status.FINISHED.name()));
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

    /** 输出节点发送节点状态和输出信息 */
    private void sendOutput(FlowData currentNode, Document p) {
        Document output = new Document();
        output.putAll(p);
        output.remove("input");
        output.remove("payload");
        Document msg = new Document("status", Node.Status.FINISHED.name()).append("output", output);
        sendNodeStatus(currentNode, msg);
    }

    /** 推送节点状态 */
    private void sendNodeStatus(FlowData currentNode, Document msg) {
        String status = msg.getString("status");
        // 若当前节点已运行完成，则检查NextNode
        if (Node.Status.FINISHED.name().equals(status)) {
            forEach(
                    getNextNode(currentNode),
                    node -> flowStatusService.addRunningNode(flowId, node.getId()));
        }
        msg.append("nodeId", currentNode.getId());
        String instanceId = Optional.ofNullable(currentNode.getParams())
                .map(p -> p.getString("instanceId"))
                .orElse(flowId);
        String flowStatus =
                ApplicationContextUtil.getBean(FlowStatusService.class)
                        .getFlowStatus(instanceId, msg);
        // MQ 已移除，状态计算仅用于内存管理
    }
}
