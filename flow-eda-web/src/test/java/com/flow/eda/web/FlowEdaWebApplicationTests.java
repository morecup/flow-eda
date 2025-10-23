package com.flow.eda.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.flow.eda.web.TestBootApplication;
import com.flow.eda.web.flow.FlowMapper;
import com.flow.eda.web.flow.node.data.NodeDataMapper;
import com.flow.eda.web.flow.node.type.NodeTypeMapper;
import com.flow.eda.web.log.LogService;
import com.flow.eda.web.flow.FlowService;
import com.flow.eda.web.flow.node.data.NodeDataService;
import com.flow.eda.web.flow.node.type.NodeTypeService;
import com.flow.eda.web.flow.status.FlowStatusService;

@SpringBootTest(classes = TestBootApplication.class, properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
class FlowEdaWebApplicationTests {

    @MockBean
    private FlowMapper flowMapper;

    @MockBean
    private NodeDataMapper nodeDataMapper;

    @MockBean
    private NodeTypeMapper nodeTypeMapper;

    @MockBean
    private LogService logService;

    @MockBean
    private FlowService flowService;

    @MockBean
    private NodeDataService nodeDataService;

    @MockBean
    private NodeTypeService nodeTypeService;

    @MockBean
    private FlowStatusService flowStatusService;

    @Test
    void contextLoads() {}
}
