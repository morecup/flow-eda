package com.flow.eda.server.flow.node.data;

import com.flow.eda.common.exception.InvalidStateException;
import com.flow.eda.common.model.FlowData;
import com.flow.eda.common.utils.MergeBuilder;
import com.flow.eda.server.flow.node.type.NodeType;
import com.flow.eda.server.flow.node.type.NodeTypeService;
import com.flow.eda.server.runtime.FlowDataRuntime;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
        flowDataRuntime.runFlowData(this.queryNodeData(flowId));
    }

    public void runNodeData(String flowId, String instanceId) {
        List<FlowData> data = this.queryNodeData(flowId);
        // 将 instanceId 透传至 Runner
        for (FlowData d : data) {
            org.bson.Document params = d.getParams();
            if (params == null) params = new org.bson.Document();
            params.put("instanceId", instanceId);
            d.setParams(params);
        }
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
