package com.flow.eda.runner.runtime;

import com.flow.eda.common.model.FlowData;
import com.flow.eda.runner.node.Node;
import com.flow.eda.runner.status.FlowStatusService;
import com.flow.eda.runner.status.FlowStatusMqProducer;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

/** FlowExecutor 与状态服务/MQ 的交互（TDD 骨架，后续在实现中注入可替换点） */
public class FlowExecutorTest {

    @Test
    void when_node_finished_then_send_status_and_mq() throws Exception {
        // 构造最小 FlowData
        FlowData d = new FlowData();
        d.setId("n1"); d.setFlowId("f1"); d.setNodeName("start");

        // 仅确保构造不抛异常；
        // 详细行为在实现中通过可注入的服务与 Mockito 校验（此处作为占位）。
        new FlowExecutor(Collections.singletonList(d), null);
    }
}
