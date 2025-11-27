package com.flow.eda.server.runtime.node.algorithm;

import com.allprs.job.center.api.enums.JobStatus;
import com.allprs.job.center.api.feign.JobCenterJobClient;
import com.flow.eda.common.exception.FlowException;
import com.flow.eda.server.common.ApplicationContextUtil;
import com.flow.eda.server.runtime.node.AbstractNode;
import com.flow.eda.server.runtime.node.NodeFunction;
import com.flow.eda.server.runtime.node.NodeVerify;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 通用算法节点：
 * 1) 将上游输入参数扁平化为字符串列表并以空格拼接；
 * 2) 携带 algorithmId 作为固定参数，调用外部算法（此处先用本地固定返回代替 feign 调用）；
 * 3) 将返回结果转换为通用参数作为下游 input 输出。
 */
public class GenericAlgorithmNode extends AbstractNode {
    private String algorithmId;
    private Object localAlgorithmParams;

    public GenericAlgorithmNode(Document params) {
        super(params);
    }

    @Override
    public void run(NodeFunction callback) {
        try {
            // 1. 准备参数
            Document in = getInput();
            Object inputAlgorithmParams = in != null ? in.get("algorithmParams") : null;

            List<String> flattened = new ArrayList<>();
            flattened.addAll(flattenValues(localAlgorithmParams));
            flattened.addAll(flattenValues(inputAlgorithmParams));
            String joined = joinBySpace(flattened);

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
            // 参数：折叠后的参数
            request.setJobParam(joined);

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

            Document out = output().append("algorithmResult", result);
            // 原样输出输入的 algorithmParams
            if (inputAlgorithmParams != null) {
                out.append("algorithmParams", inputAlgorithmParams);
            }
            callback.callback(out);

        } catch (Exception e) {
            throw FlowException.wrap(e);
        }
    }

    @Override
    protected void verify(Document params) {
        this.algorithmId = params.getString("algorithmId");
        NodeVerify.notBlank(this.algorithmId, "algorithmId");
        // 保存本节点的 algorithmParams（可选）
        this.localAlgorithmParams = params.get("algorithmParams");
    }

    private List<String> flattenValues(Object root) {
        List<String> out = new ArrayList<>();
        flatten(root, out);
        return out;
    }

    private void flatten(Object obj, List<String> out) {
        if (obj == null) return;
        if (obj instanceof Document) {
            for (Map.Entry<String, Object> e : ((Document) obj).entrySet()) {
                out.add(String.valueOf(e.getKey()));
                flatten(e.getValue(), out);
            }
            return;
        }
        if (obj instanceof Map) {
            for (Map.Entry<?, ?> e : ((Map<?, ?>) obj).entrySet()) {
                out.add(String.valueOf(e.getKey()));
                flatten(e.getValue(), out);
            }
            return;
        }
        if (obj instanceof Iterable) {
            for (Object v : (Iterable<?>) obj) flatten(v, out);
            return;
        }
        if (obj.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < len; i++) flatten(java.lang.reflect.Array.get(obj, i), out);
            return;
        }
        out.add(String.valueOf(obj));
    }

    private String joinBySpace(List<String> values) {
        return String.join(" ", values);
    }
}
