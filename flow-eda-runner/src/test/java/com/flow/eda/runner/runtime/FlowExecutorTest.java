package com.flow.eda.runner.runtime;

import com.flow.eda.common.model.FlowData;
import com.flow.eda.runner.status.FlowStatusService;
import com.flow.eda.runner.utils.ApplicationContextUtil;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Collections;

public class FlowExecutorTest {

    private FlowStatusService statusService;

    @BeforeEach
    void setupContext() {
        StaticApplicationContext context = new StaticApplicationContext();
        statusService = Mockito.mock(FlowStatusService.class);
        context.getBeanFactory().registerSingleton("flowStatusService", statusService);
        ApplicationContextUtil.setApplicationContext(context);

        Mockito.when(statusService.getFlowStatus(Mockito.eq("flow-1"), Mockito.any(Document.class)))
                .thenAnswer(invocation -> {
                    Document doc = invocation.getArgument(1, Document.class);
                    return doc.getString("status");
                });
    }

    @Test
    void when_node_finished_then_send_status() {
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
