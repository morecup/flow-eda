package com.flow.eda.server.runtime.node.algorithm;

import com.allprs.datamanage.api.RemoteDataIngestService;
import com.allprs.datamanage.api.dto.CreateAbsolutePathIngestTaskDTO;
import com.allprs.datamanage.api.dto.CreateIngestTaskDTO;
import com.allprs.datamanage.api.dto.RemoteResult;
import com.flow.eda.common.exception.FlowException;
import com.flow.eda.server.common.ApplicationContextUtil;
import com.flow.eda.server.runtime.node.AbstractNode;
import com.flow.eda.server.runtime.node.NodeFunction;
import com.flow.eda.server.runtime.node.NodeVerify;
import org.bson.Document;

/**
 * 数据入库节点
 * 调用 RemoteDataIngestService.createAndExecuteTask 创建入库任务并同步等待完成
 */
public class DataIngestNode extends AbstractNode {

    private String dataType;
    private Long configId;
    private Long mappingId;
    private String ingestDataPath;

    public DataIngestNode(Document params) {
        super(params);
    }

    @Override
    protected void verify(Document params) {
        // 1. 从节点配置获取用户配置的参数
        this.dataType = params.getString("dataType");
        NodeVerify.notBlank(this.dataType, "dataType");

        String configIdStr = params.getString("configId");
        NodeVerify.notBlank(configIdStr, "configId");
        this.configId = Long.parseLong(configIdStr);

        String mappingIdStr = params.getString("mappingId");
        NodeVerify.notBlank(mappingIdStr, "mappingId");
        this.mappingId = Long.parseLong(mappingIdStr);

        // 2. 从上游节点输出获取 ingestDataPath
        Document input = getInput();
        this.ingestDataPath = getIngestDataPath(params, input);
        NodeVerify.notBlank(this.ingestDataPath, "ingestDataPath");
    }

    private String getIngestDataPath(Document params, Document input) {
        // 优先从 params 中获取（支持占位符替换后的值）
        String path = params.getString("ingestDataPath");
        if (path != null && !path.trim().isEmpty()) {
            return path;
        }
        // 从上游输入获取固定字段名 ingestDataPath
        if (input != null) {
            Object v = input.get("ingestDataPath");
            if (v instanceof String && !((String) v).isEmpty()) {
                return (String) v;
            }
        }
        return null;
    }

    @Override
    public void run(NodeFunction callback) {
        try {
            // 1. 获取 Feign 客户端
            RemoteDataIngestService ingestService = ApplicationContextUtil.getBean(RemoteDataIngestService.class);

            // 2. 构建请求
            CreateAbsolutePathIngestTaskDTO dto = new CreateAbsolutePathIngestTaskDTO();
            dto.setTaskName("ingest_task_" + System.currentTimeMillis());
            dto.setAbsoluteSourcePath(this.ingestDataPath);
            dto.setDataType(this.dataType);
            dto.setConfigId(this.configId);
            dto.setMappingId(this.mappingId);

            // 3. 同步调用（接口本身是同步阻塞的）
            RemoteResult<Long> result = ingestService.createAndExecuteTaskByAbsolutePath(dto);

            // 4. 处理结果 - 失败时抛异常中断流程
            if (result == null || !result.isSuccess()) {
                String errorMsg = result != null ? result.getMsg() : "Remote call failed";
                throw new FlowException("Data ingest task failed: " + errorMsg);
            }

            Long taskId = result.getData();

            // 5. 返回结果
            setStatus(Status.FINISHED);
            Document output = output()
                    .append("ingestTaskId", taskId)
                    .append("ingestDataPath", this.ingestDataPath)
                    .append("dataType", this.dataType);
            callback.callback(output);

        } catch (Exception e) {
            throw FlowException.wrap(e);
        }
    }
}
