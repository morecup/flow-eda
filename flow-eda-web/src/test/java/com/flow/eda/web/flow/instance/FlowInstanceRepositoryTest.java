package com.flow.eda.web.flow.instance;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = com.flow.eda.web.TestBootApplication.class)
@MapperScan("com.flow.eda.web.flow.instance")
@Import(FlowInstanceRepository.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:flow_instance;MODE=MYSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.hikari.maximum-pool-size=5",
        "spring.datasource.hikari.minimum-idle=1",
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false",
        "mybatis.mapper-locations=classpath:/mapper/*.xml",
        "mybatis.type-handlers-package="
})
@Transactional
@Sql(scripts = "classpath:db/migration/V20251022__init_flow_instance_tables.sql")
class FlowInstanceRepositoryTest {

    @Autowired
    private FlowInstanceRepository repository;

    @Test
    void should_persist_and_query_flow_instance_metadata() {
        FlowInstanceDO instance = new FlowInstanceDO();
        instance.setId("inst-001");
        instance.setFlowId("flow-001");
        instance.setStatus("RUNNING");
        instance.setTriggerUser("tester");
        instance.setStartTime(LocalDateTime.now());
        instance.setCreatedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());

        repository.saveInstance(instance);

        FlowInstanceDO stored =
                repository.findInstanceById("inst-001").orElseThrow(() -> new AssertionError("instance not found"));

        assertEquals("flow-001", stored.getFlowId());
        assertEquals("RUNNING", stored.getStatus());
        assertEquals("tester", stored.getTriggerUser());
    }

    @Test
    void should_persist_and_query_instance_nodes() {
        FlowInstanceNodeDO node1 = new FlowInstanceNodeDO();
        node1.setInstanceId("inst-002");
        node1.setNodeId("n1");
        node1.setNodeName("开始节点");
        node1.setNodeType("start");
        node1.setStatus("FINISHED");
        node1.setInputJson("{}");
        node1.setOutputJson("{\"result\":1}");
        node1.setDurationMs(10L);
        node1.setCreatedAt(LocalDateTime.now());

        FlowInstanceNodeDO node2 = new FlowInstanceNodeDO();
        node2.setInstanceId("inst-002");
        node2.setNodeId("n2");
        node2.setNodeName("处理节点");
        node2.setNodeType("service");
        node2.setStatus("RUNNING");
        node2.setInputJson("{\"a\":1}");
        node2.setDurationMs(5L);
        node2.setCreatedAt(LocalDateTime.now());

        repository.saveNodes(Arrays.asList(node1, node2));

        List<FlowInstanceNodeDO> nodes = repository.findNodesByInstanceId("inst-002");
        assertEquals(2, nodes.size());
        assertTrue(nodes.stream().anyMatch(n -> "n1".equals(n.getNodeId()) && "FINISHED".equals(n.getStatus())));
    }

    @Test
    void should_persist_and_query_instance_logs() {
        FlowInstanceLogDO log1 = new FlowInstanceLogDO();
        log1.setInstanceId("inst-003");
        log1.setNodeId("n1");
        log1.setLevel("INFO");
        log1.setCategory("NODE_IO");
        log1.setMessage("start node");
        log1.setPayloadJson("{}");
        log1.setLogTime(LocalDateTime.now());
        log1.setCreatedAt(LocalDateTime.now());

        FlowInstanceLogDO log2 = new FlowInstanceLogDO();
        log2.setInstanceId("inst-003");
        log2.setNodeId("n2");
        log2.setLevel("ERROR");
        log2.setCategory("NODE_RUN");
        log2.setMessage("failed");
        log2.setPayloadJson("{\"code\":500}");
        log2.setLogTime(LocalDateTime.now());
        log2.setCreatedAt(LocalDateTime.now());

        repository.saveLogs(Arrays.asList(log1, log2));

        List<FlowInstanceLogDO> logs = repository.findLogsByInstanceId("inst-003");
        assertEquals(2, logs.size());
        assertTrue(logs.stream().anyMatch(l -> "ERROR".equals(l.getLevel()) && "failed".equals(l.getMessage())));
    }

    @Test
    void should_delete_instances_by_flow_and_related_data() {
        FlowInstanceDO instance = new FlowInstanceDO();
        instance.setId("inst-004");
        instance.setFlowId("flow-del");
        instance.setStatus("FAILED");
        repository.saveInstance(instance);

        FlowInstanceNodeDO node = new FlowInstanceNodeDO();
        node.setInstanceId("inst-004");
        node.setNodeId("node-1");
        node.setStatus("FAILED");
        node.setInputJson("{}");
        node.setCreatedAt(LocalDateTime.now());
        repository.saveNodes(Collections.singletonList(node));

        FlowInstanceLogDO log = new FlowInstanceLogDO();
        log.setInstanceId("inst-004");
        log.setLevel("WARN");
        log.setMessage("fail");
        log.setLogTime(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        repository.saveLogs(Collections.singletonList(log));

        repository.deleteInstancesByFlowId("flow-del");

        assertTrue(repository.findInstancesByFlowId("flow-del").isEmpty());
        assertTrue(repository.findNodesByInstanceId("inst-004").isEmpty());
        assertTrue(repository.findLogsByInstanceId("inst-004").isEmpty());
    }
}
