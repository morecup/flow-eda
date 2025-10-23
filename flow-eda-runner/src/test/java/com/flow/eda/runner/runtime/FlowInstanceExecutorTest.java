package com.flow.eda.runner.runtime;

import com.flow.eda.common.model.FlowData;
import com.flow.eda.runner.status.FlowStatusService;
import com.flow.eda.runner.utils.ApplicationContextUtil;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD: 实例化运行应以 instanceId 为主键进行状态上报。
 * 期望：
 * 1) FlowExecutor 在运行时应传递 instanceId 给 FlowStatusService，而非 flowId。
 * 2) 节点参数中应包含 instanceId（用于后续节点快照记录）。
 */
public class FlowInstanceExecutorTest {

    private FlowStatusService statusService;

    @BeforeEach
    void setupContext() {
        StaticApplicationContext context = new StaticApplicationContext();
        statusService = Mockito.mock(FlowStatusService.class);
        context.getBeanFactory().registerSingleton("flowStatusService", statusService);
        ApplicationContextUtil.setApplicationContext(context);

        Mockito.when(statusService.getFlowStatus(Mockito.anyString(), Mockito.any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(1, Document.class).getString("status"));
    }

    @Test
    void should_use_instance_id_for_status_and_params() {
        // 准备一个只有输出节点的最小流程
        FlowData node = new FlowData();
        node.setId("n1");
        node.setFlowId("flow-1");
        node.setNodeName("output");
        node.setType("output");
        Document params = new Document();
        params.put("instanceId", "inst-1");
        node.setParams(params);

        FlowExecutor executor = new FlowExecutor(Collections.singletonList(node), null);
        executor.start(node);

        // 校验：状态上报时应使用 instanceId（inst-1），而不是 flowId（flow-1）
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(statusService, Mockito.atLeastOnce())
                .getFlowStatus(idCaptor.capture(), Mockito.any(Document.class));
        assertTrue(idCaptor.getAllValues().contains("inst-1"),
                "FlowStatusService.getFlowStatus 应以 instanceId 标识实例");

        // 节点参数应包含 instanceId（供后续写入节点快照使用）
        assertTrue(node.getParams() != null && node.getParams().containsKey("instanceId"),
                "节点参数应包含 instanceId");
    }
}
