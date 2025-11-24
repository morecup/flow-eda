package com.flow.eda.server.flow.node.data;

import com.flow.eda.common.exception.InvalidParameterException;
import com.flow.eda.common.exception.InvalidStateException;
import com.flow.eda.common.http.Result;
import com.flow.eda.common.utils.CheckFieldUtil;
import com.flow.eda.server.log.OperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.flow.eda.common.utils.CollectionUtil.isEmpty;

@RestController
@RequestMapping({"/api/v1", "/api/v1/feign"})
public class NodeDataController {
    @Autowired private NodeDataService nodeDataService;

    @OperationLog
    @GetMapping("/node/data")
    public Result<List<NodeData>> getNodeData(
            @RequestParam String flowId, @RequestParam(required = false) String version) {
        return Result.of(nodeDataService.getNodeData(flowId, version));
    }

    @OperationLog
    @PostMapping("/node/data")
    public Result<String> setNodeData(@RequestBody List<NodeData> data) {
        this.check(data);
        nodeDataService.updateNodeData(data);
        return Result.ok();
    }

    @OperationLog
    @GetMapping("/node/data/version")
    public Result<List<String>> getVersions(@RequestParam String flowId) {
        return Result.ok(nodeDataService.getVersions(flowId));
    }

    @OperationLog
    @PostMapping("/node/data/version")
    public Result<String> saveVersion(
            @RequestParam String version, @RequestBody List<NodeData> data) {
        this.check(data);
        if (version.length() > 32) {
            throw new InvalidParameterException("The version name is too long");
        }
        data.forEach(node -> node.setVersion(version));
        nodeDataService.saveNodeData(data);
        return Result.ok();
    }

    @OperationLog
    @PostMapping("/node/data/run")
    public Result<String> runNodeData(@RequestBody NodeDataRunRequest request) {
        if (request == null || request.getFlowId() == null || request.getFlowId().isEmpty()) {
            throw new InvalidParameterException("FlowId is required");
        }
        String instanceId = request.getInstanceId();
        if (instanceId == null || instanceId.isEmpty()) {
            instanceId = UUID.randomUUID().toString();
        }
        nodeDataService.runNodeData(request.getFlowId(), instanceId, request.getPayload());
        return Result.ok(instanceId);
    }

    /** 校验节点数据的正确性 */
    private void check(List<NodeData> data) {
        if (isEmpty(data)) {
            throw new InvalidStateException("The flow node data is empty");
        }
        // 校验必填参数
        for (NodeData node : data) {
            CheckFieldUtil.missingProperty("id", node.getId());
            CheckFieldUtil.missingProperty("flowId", node.getFlowId());
        }
    }
}
