package com.flow.eda.server.runtime.node.standard;

import com.flow.eda.server.runtime.node.AbstractNode;
import com.flow.eda.server.runtime.node.NodeFunction;
import org.bson.Document;

/**
 * 标准化参数节点：仅将上游节点传来的自定义参数（input）向下游传递，
 * 不附带当前节点的任何其他参数，实现标准化传递。
 */
public class StandardizeParamNode extends AbstractNode {

    public StandardizeParamNode(Document params) {
        super(params);
    }

    @Override
    public void run(NodeFunction callback) {
        setStatus(Status.FINISHED);
        callback.callback(output());
    }

    @Override
    protected void verify(Document params) {
        // 无需校验参数
    }
}
