package com.flow.eda.server.runtime.node.algorithm;

import com.allprs.job.center.api.enums.JobStatus;
import com.allprs.job.center.api.feign.JobCenterJobClient;
import com.flow.eda.common.exception.FlowException;
import com.flow.eda.server.common.ApplicationContextUtil;
import com.flow.eda.server.runtime.node.AbstractNode;
import com.flow.eda.server.runtime.node.NodeFunction;
import com.flow.eda.server.runtime.node.NodeVerify;
import org.bson.Document;

import java.util.concurrent.TimeUnit;

/**
 * 通用算法节点：
 * 1) 透传上游 input 到下游，保证占位符链路可用（与 XmlGenerateNode 行为一致）；
 * 2) 使用 algorithmInput 作为算法入参，直接透传给 Feign（不要求 JSON 格式，支持 ${} 占位符）；
 * 3) 提交任务并轮询状态，将结果输出为 algorithmResult。
 */
public class GenericAlgorithmNode extends AbstractNode {
    private String algorithmId;
    private String algorithmInput;

    public GenericAlgorithmNode(Document params) {
        super(params);
    }

    @Override
    public void run(NodeFunction callback) {
        try {
            // 1. 准备参数
            String jobParam = this.algorithmInput;

            // 2. 获取 Feign 客户端
            JobCenterJobClient jobClient = ApplicationContextUtil.getBean(JobCenterJobClient.class);

            // 3. 构建请求
            JobCenterJobClient.JobCreateRequest request = new JobCenterJobClient.JobCreateRequest();
            try {
                request.setStrategyId(Long.parseLong(algorithmId));
            } catch (NumberFormatException e) {
                throw new FlowException("Invalid algorithmId format, must be Long: " + algorithmId);
            }
            // 任务名称：算法ID + 当前时间
            request.setName(algorithmId + "_" + System.currentTimeMillis());
            // 参数：算法入参，直接透传给 Feign
            request.setJobParam(jobParam);

            // 4. 提交任务
            long jobId = jobClient.addAndReturnId(request);

            // 5. 轮询状态
            JobStatus finalStatus;
            while (true) {
                JobStatus status = jobClient.getStatus(jobId);
                if (status == JobStatus.SUCCESS) {
                    finalStatus = status;
                    break;
                }
                if (status == JobStatus.FAIL || status == JobStatus.STOP || status == JobStatus.CANCEL) {
                    throw new FlowException("Job failed with status: " + status.getDesc());
                }
                // 其他状态继续等待 (ON_QUEUE, ON_DEL, PAUSE)
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw FlowException.wrap(e, "Job polling interrupted");
                }
            }

            // 6. 返回结果
            setStatus(Status.FINISHED);
            // 返回 jobID 和状态
            Document result = new Document("jobId", jobId)
                    .append("status", finalStatus.getCode())
                    .append("desc", finalStatus.getDesc());

            Document out = output();

            // 始终透传上游 input，保证下游节点可继续使用占位符取值
            Document passthroughInput = new Document();
            passthroughInput.putAll(getInput());
            Document payloadInput = out.get("input", Document.class);
            if (payloadInput != null && !payloadInput.isEmpty()) {
                passthroughInput.putAll(payloadInput);
            }
            out.put("input", passthroughInput);

            out.append("algorithmInput", jobParam);
            out.append("algorithmResult", result);
            callback.callback(out);

        } catch (Exception e) {
            throw FlowException.wrap(e);
        }
    }

    @Override
    protected void verify(Document params) {
        this.algorithmId = params.getString("algorithmId");
        NodeVerify.notBlank(this.algorithmId, "algorithmId");

        String algorithmInput = params.getString("algorithmInput");
        NodeVerify.notBlank(algorithmInput, "algorithmInput");
        this.algorithmInput = algorithmInput;
    }

}
