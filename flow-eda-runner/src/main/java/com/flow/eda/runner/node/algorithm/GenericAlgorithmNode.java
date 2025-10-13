package com.flow.eda.runner.node.algorithm;

import com.flow.eda.common.exception.FlowException;
import com.flow.eda.runner.node.AbstractNode;
import com.flow.eda.runner.node.NodeFunction;
import com.flow.eda.runner.node.NodeVerify;
import org.bson.Document;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

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
            Document in = getInput();
            Object inputAlgorithmParams = in != null ? in.get("algorithmParams") : null;

            List<String> flattened = new ArrayList<>();
            flattened.addAll(flattenValues(localAlgorithmParams));
            flattened.addAll(flattenValues(inputAlgorithmParams));
            String joined = joinBySpace(flattened);
            Document feignRes = callFeignStub(algorithmId, joined);
            Document common = toCommonParams(feignRes);
            setStatus(Status.FINISHED);
            callback.callback(output().append("algorithmResult", common));
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

    /** 固定返回的 feign 调用占位实现 */
    private Document callFeignStub(String algoId, String payload) {
        return new Document("code", 0)
                .append("algoId", algoId)
                .append("data", new Document("echo", payload).append("length", payload.length()).append("algorithmId", algoId));
    }

    /** 将外部返回转为通用参数结构 */
    private Document toCommonParams(Document res) {
        Document common = new Document();
        if (res != null) {
            Object data = res.get("data");
            if (data instanceof Document) {
                common.putAll((Document) data);
            } else if (data != null) {
                common.append("data", data);
            }
        }
        return common;
    }
}
