package com.flow.eda.web.flow.instance;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FlowInstanceMapper {

    int insertInstance(FlowInstanceDO instance);

    int updateInstance(FlowInstanceDO instance);

    FlowInstanceDO selectInstanceById(String instanceId);

    List<FlowInstanceDO> selectInstancesByFlowId(String flowId);

    void insertNodes(List<FlowInstanceNodeDO> nodes);

    List<FlowInstanceNodeDO> selectNodesByInstanceId(String instanceId);

    void insertLogs(List<FlowInstanceLogDO> logs);

    List<FlowInstanceLogDO> selectLogsByInstanceId(String instanceId);

    void deleteInstancesByFlowId(String flowId);

    void deleteNodesByInstanceIds(List<String> instanceIds);

    void deleteLogsByInstanceIds(List<String> instanceIds);
}
