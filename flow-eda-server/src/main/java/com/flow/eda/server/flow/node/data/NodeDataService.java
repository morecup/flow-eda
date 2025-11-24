package com.flow.eda.server.flow.node.data;

import com.flow.eda.common.exception.InvalidStateException;
import com.flow.eda.common.model.FlowData;
import com.flow.eda.common.utils.MergeBuilder;
import com.flow.eda.server.flow.node.type.NodeType;
import com.flow.eda.server.flow.node.type.NodeTypeService;
import com.flow.eda.server.runtime.node.NodeTypeEnum;
import com.flow.eda.server.runtime.FlowDataRuntime;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.flow.eda.common.utils.CollectionUtil.isEmpty;

@Service
public class NodeDataService {
    @Lazy
    @Autowired private FlowDataRuntime flowDataRuntime;
    @Autowired private NodeDataMapper nodeDataMapper;
    @Autowired private NodeTypeService nodeTypeService;

    public List<NodeData> getNodeData(String flowId, @Nullable String version) {
        List<NodeData> list = nodeDataMapper.findByFlowId(flowId, version);
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        // 封装节点类型信息
        return MergeBuilder.source(list, NodeData::getTypeId)
                .target(nodeTypeService::findByIds, NodeType::getId)
                .mergeS(NodeData::setNodeType);
    }

    public List<String> getVersions(String flowId) {
        return nodeDataMapper.findVersionsByFlowId(flowId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateNodeData(List<NodeData> data) {
        String flowId = data.get(0).getFlowId();
        nodeDataMapper.deleteDataByFlowId(flowId);
        nodeDataMapper.insert(data);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveNodeData(List<NodeData> data) {
        nodeDataMapper.insert(data);
    }

    public void runNodeData(String flowId) {
        this.runNodeData(flowId, null, null);
    }

    public void runNodeData(String flowId, String instanceId) {
        this.runNodeData(flowId, instanceId, null);
    }

    public void runNodeData(String flowId, String instanceId, Map<String, Object> payload) {
        List<FlowData> data = this.queryNodeData(flowId);
        this.injectPayload(data, payload);
        this.injectInstanceId(data, instanceId);
        flowDataRuntime.runFlowData(data);
    }

    public List<FlowData> queryNodeData(String flowId) {
        List<NodeData> list = this.getNodeData(flowId, null);
        if (isEmpty(list)) {
            throw new InvalidStateException("The flow data is empty, cannot be run");
        }
        List<FlowData> data = new ArrayList<>();
        list.forEach(n -> data.add(this.convert(n)));
        return data;
    }

    private void injectPayload(List<FlowData> data, Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return;
        }
        Document payloadDoc = new Document(payload);
        data.stream()
                .filter(d -> NodeTypeEnum.START.getType().equals(d.getType()))
                .forEach(d -> {
                    Document params = d.getParams();
                    if (params == null) {
                        params = new Document();
                    }
                    params.put("payload", new Document(payloadDoc));
                    d.setParams(params);
                });
    }

    private void injectInstanceId(List<FlowData> data, String instanceId) {
        if (instanceId == null || instanceId.isEmpty()) {
            return;
        }
        for (FlowData d : data) {
            Document params = d.getParams();
            if (params == null) {
                params = new Document();
            }
            params.put("instanceId", instanceId);
            d.setParams(params);
        }
    }

    private FlowData convert(NodeData nodeData) {
        FlowData flowData = new FlowData();
        flowData.setId(nodeData.getId());
        flowData.setFlowId(nodeData.getFlowId());
        flowData.setNodeName(nodeData.getNodeName());
        if (nodeData.getNodeType() != null) {
            flowData.setType(nodeData.getNodeType().getType());
        }
        Document params = null;
        // 将节点payload内容添加到params参数中
        if (nodeData.getParams() != null) {
            params = nodeData.getParams();
            if (nodeData.getPayload() != null) {
                params.append("payload", nodeData.getPayload());
            }
        } else if (nodeData.getPayload() != null) {
            params = new Document("payload", nodeData.getPayload());
        }
        flowData.setParams(params);
        flowData.setFrom(nodeData.getFrom());
        flowData.setTo(nodeData.getTo());
        return flowData;
    }
}
