package com.flow.eda.runner.runtime;

import com.flow.eda.common.model.FlowData;
import com.flow.eda.common.utils.CollectionUtil;
import com.flow.eda.runner.node.Node;
import com.flow.eda.runner.node.NodeTypeEnum;
import com.flow.eda.runner.status.FlowStatusMqProducer;
import com.flow.eda.runner.status.FlowStatusService;
import com.flow.eda.runner.utils.FlowLogs;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.flow.eda.common.utils.CollectionUtil.*;
import static com.flow.eda.runner.runtime.FlowThreadPool.getThreadPool;

@Service
public class FlowDataRuntime {
    @Autowired private FlowStatusMqProducer mqProducer;
    @Autowired private FlowStatusService flowStatusService;

    /** 运行流程 */
    public void runFlowData(List<FlowData> data) {
        String flowId =
                Objects.requireNonNull(findFirst(data, n -> n.getFlowId() != null)).getFlowId();
        // 将流数据分为开始节点和定时器节点进行分别执行
        List<FlowData> starts = filter(data, this::isStartNode);
        // 获取作为起始节点的定时器节点
        List<FlowData> timers = filter(data, d -> isTimerNode(d) && isStart(d.getId(), data));
        FlowLogs.info(flowId, "start running flow {}", flowId);
        // 实时计算流程运行状态
        flowStatusService.startRun(flowId, starts, timers);
        // 异步执行
        forEach(
                starts,
                d -> getThreadPool(flowId).execute(() -> new FlowExecutor(data, null).start(d)));
        forEach(
                timers,
                d -> getThreadPool(flowId).execute(() -> new FlowExecutor(data, null).start(d)));
    }

    /** 停止流程 */
    public void stopFlowData(String flowId) {
        FlowLogs.info(flowId, "stop flow {}", flowId);
        FlowThreadPool.shutdownThreadPool(flowId);
        FlowThreadPool.shutdownSchedulerPool(flowId);
        FlowBlockNodePool.shutdownBlockNode(flowId);
        // 手动停止后，通知流程完成（失败中断的节点不再逐个推送）
        mqProducer.sendFlowStatus(flowId, Node.Status.FINISHED.name());
    }

    /** 清理流程运行的缓存数据 */
    public void clearFlowData(String flowId) {
        FlowLogs.info(flowId, "clean flow {} cached data", flowId);
        FlowThreadPool.shutdownThreadPool(flowId);
        FlowThreadPool.shutdownSchedulerPool(flowId);
        FlowBlockNodePool.shutdownBlockNode(flowId);
        flowStatusService.clear(flowId);
    }

    private boolean isStartNode(FlowData d) {
        return NodeTypeEnum.START.getType().equals(d.getType());
    }

    private boolean isTimerNode(FlowData d) {
        return NodeTypeEnum.TIMER.getType().equals(d.getType());
    }

    /** 判断当前节点是否为起始节点（即最上游节点） */
    private boolean isStart(String id, List<FlowData> list) {
        return list.stream().noneMatch(d -> id.equals(d.getTo()));
    }
}
