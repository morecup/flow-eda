package com.flow.eda.server.runtime.node.algorithm;

import com.flow.eda.server.runtime.node.AbstractNode;
import com.flow.eda.server.runtime.node.NodeFunction;
import com.flow.eda.server.runtime.node.NodeVerify;
import org.bson.Document;

import java.util.Map;

public class TifPathNode extends AbstractNode {
    private String inputTifPath;
    private String outputTifPath;

    public TifPathNode(Document params) {
        super(params);
    }

    @Override
    public void run(NodeFunction function) {
        setStatus(Status.FINISHED);
        function.callback(output().append("outputTifPath", this.outputTifPath));
    }

    @Override
    protected void verify(Document params) {
        String path = getPath(params);
        if (path == null || path.trim().isEmpty()) {
            Document input = params.get("input", Document.class);
            path = getPath(input);
        }
        NodeVerify.notBlank(path, "inputTifPath");
        this.inputTifPath = path;
        this.outputTifPath = parent(path);
        NodeVerify.notBlank(this.outputTifPath, "outputTifPath");
    }

    private String getPath(Document doc) {
        if (doc == null || doc.isEmpty()) return null;
        Object v = doc.get("inputTifPath");
        if (v instanceof String) return (String) v;
        for (Map.Entry<String, Object> e : doc.entrySet()) {
            if (e.getKey() != null && e.getKey().contains("inputTifPath") && e.getValue() instanceof String) {
                return (String) e.getValue();
            }
        }
        return null;
    }

    private String parent(String p) {
        String s = p.replace('\\', '/');
        while (s.endsWith("/") && s.length() > 1) {
            s = s.substring(0, s.length() - 1);
        }
        int i = s.lastIndexOf('/');
        if (i > 0) return s.substring(0, i);
        return s;
    }
}
