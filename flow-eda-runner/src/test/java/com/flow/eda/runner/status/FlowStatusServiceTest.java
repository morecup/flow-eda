package com.flow.eda.runner.status;

import com.flow.eda.common.model.FlowData;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/** FlowStatusService 基本行为单元测试（TDD 骨架） */
public class FlowStatusServiceTest {

    @Test
    void startRun_and_getFlowStatus_and_clear() {
        FlowStatusService s = new FlowStatusService();
        String flowId = "f1";
        FlowData n1 = new FlowData(); n1.setId("n1");
        FlowData n2 = new FlowData(); n2.setId("n2");

        s.startRun(flowId, Arrays.asList(n1), Arrays.asList(n2));

        // 初始为 RUNNING
        Document msg = new Document("nodeId", "n1").append("status", "RUNNING");
        String st1 = s.getFlowStatus(flowId, msg);
        Assertions.assertEquals("RUNNING", st1);

        // 完成 n1 仍可能 RUNNING（n2 未完成）
        msg.put("status", "FINISHED");
        String st2 = s.getFlowStatus(flowId, msg);
        Assertions.assertEquals("RUNNING", st2);

        // 完成 n2 -> FINISHED
        Document msg2 = new Document("nodeId", "n2").append("status", "FINISHED");
        String st3 = s.getFlowStatus(flowId, msg2);
        Assertions.assertEquals("FINISHED", st3);

        // clear 不抛异常
        s.clear(flowId);
    }
}
