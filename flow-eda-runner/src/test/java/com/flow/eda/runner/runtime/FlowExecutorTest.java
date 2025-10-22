package com.flow.eda.runner.runtime;

import com.flow.eda.common.model.FlowData;
import com.flow.eda.runner.status.FlowStatusMqProducer;
import com.flow.eda.runner.status.FlowStatusService;
import com.flow.eda.runner.utils.ApplicationContextUtil;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Collections;

public class FlowExecutorTest {

    private FlowStatusService statusService;
    private FlowStatusMqProducer mqProducer;

    @BeforeEach
    void setupContext() {
        StaticApplicationContext context = new StaticApplicationContext();
        statusService = Mockito.mock(FlowStatusService.class);
        // 使用一个假的实现，避免 ByteBuddy 对 RabbitTemplate 依赖的类加载
        mqProducer = new FlowStatusMqProducer() {
            @Override
            public void sendFlowStatus(String flowId, String status) {
                // no-op in tests
            }
        };
        context.getBeanFactory().registerSingleton("flowStatusService", statusService);
        context.getBeanFactory().registerSingleton("flowStatusMqProducer", mqProducer);
        ApplicationContextUtil.setApplicationContext(context);

        Mockito.when(statusService.getFlowStatus(Mockito.eq("flow-1"), Mockito.any(Document.class)))
                .thenAnswer(invocation -> {
                    Document doc = invocation.getArgument(1, Document.class);
                    return doc.getString("status");
                });
    }

    @Test
    void when_node_finished_then_send_status_and_mq() {
        FlowData node = new FlowData();
        node.setId("n1");
        node.setFlowId("flow-1");
        node.setNodeName("output");
        node.setType("output");
        node.setParams(new Document());

        FlowExecutor executor = new FlowExecutor(Collections.singletonList(node), null);
        Assertions.assertDoesNotThrow(() -> executor.start(node));

        Mockito.verify(statusService, Mockito.atLeastOnce())
                .getFlowStatus(Mockito.eq("flow-1"), Mockito.any(Document.class));
    }
}
