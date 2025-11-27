package com.flow.eda.server.runtime.node;

import com.flow.eda.common.exception.InternalException;
import com.flow.eda.common.model.FlowData;
import com.flow.eda.server.runtime.node.condition.ConditionNode;
import com.flow.eda.server.runtime.node.delay.DelayNode;
import com.flow.eda.server.runtime.node.algorithm.TifPathNode;
import com.flow.eda.server.runtime.node.algorithm.GenericAlgorithmNode;
import com.flow.eda.server.runtime.node.algorithm.DataIngestNode;
import com.flow.eda.server.runtime.node.standard.StandardizeParamNode;
import com.flow.eda.server.runtime.node.email.EmailNode;
import com.flow.eda.server.runtime.node.http.receive.HttpReceiveNode;
import com.flow.eda.server.runtime.node.http.request.HttpRequestNode;
import com.flow.eda.server.runtime.node.http.response.HttpResponseNode;
import com.flow.eda.server.runtime.node.mongodb.MongodbNode;
import com.flow.eda.server.runtime.node.mqtt.pub.PubMqttNode;
import com.flow.eda.server.runtime.node.mqtt.sub.SubMqttNode;
import com.flow.eda.server.runtime.node.mysql.MysqlNode;
import com.flow.eda.server.runtime.node.output.OutputNode;
import com.flow.eda.server.runtime.node.parser.html.HtmlParserNode;
import com.flow.eda.server.runtime.node.parser.json.JsonParserNode;
import com.flow.eda.server.runtime.node.parser.xml.XmlParserNode;
import com.flow.eda.server.runtime.node.postgresql.PostgresqlNode;
import com.flow.eda.server.runtime.node.redis.RedisNode;
import com.flow.eda.server.runtime.node.sequence.SequenceNode;
import com.flow.eda.server.runtime.node.splice.SpliceNode;
import com.flow.eda.server.runtime.node.split.SplitNode;
import com.flow.eda.server.runtime.node.start.StartNode;
import com.flow.eda.server.runtime.node.subflow.SubflowNode;
import com.flow.eda.server.runtime.node.subflow.input.SubInputNode;
import com.flow.eda.server.runtime.node.subflow.output.SubOutputNode;
import com.flow.eda.server.runtime.node.timer.TimerNode;
import com.flow.eda.server.runtime.node.valve.ValveNode;
import lombok.Getter;

/** 节点类型 */
@Getter
public enum NodeTypeEnum {
    /** 基础节点 */
    START("start", StartNode.class),
    OUTPUT("output", OutputNode.class),
    TIMER("timer", TimerNode.class),
    DELAY("delay", DelayNode.class),

    /** 运算节点 */
    CONDITION("condition", ConditionNode.class),
    SEQUENCE("sequence", SequenceNode.class),
    SPLICE("splice", SpliceNode.class),
    SPLIT("split", SplitNode.class),
    VALVE("valve", ValveNode.class),
    STANDARDIZE("standardize", StandardizeParamNode.class),
    GENERIC_ALGO("generic_algo", GenericAlgorithmNode.class),
    TIF_PATH("tif_path", TifPathNode.class),
    DATA_INGEST("data_ingest", DataIngestNode.class),

    /** 解析节点 */
    JSON_PARSER("json_parser", JsonParserNode.class),
    HTML_PARSER("html_parser", HtmlParserNode.class),
    XML_PARSER("xml_parser", XmlParserNode.class),

    /** 网络节点 */
    HTTP_REQUEST("http_request", HttpRequestNode.class),
    HTTP_RECEIVE("http_receive", HttpReceiveNode.class),
    HTTP_RESPONSE("http_response", HttpResponseNode.class),
    MQTT_SUB("mqtt_sub", SubMqttNode.class),
    MQTT_PUB("mqtt_pub", PubMqttNode.class),
    EMAIL("email", EmailNode.class),

    /** 数据库节点 */
    MYSQL("mysql", MysqlNode.class),
    MONGODB("mongodb", MongodbNode.class),
    REDIS("redis", RedisNode.class),
    POSTGRESQL("postgresql", PostgresqlNode.class),

    /** 子流程节点 */
    SUBFLOW("subflow", SubflowNode.class),
    SUB_INPUT("sub_input", SubInputNode.class),
    SUB_OUTPUT("sub_output", SubOutputNode.class),
    ;
    private final String type;
    private final Class<? extends Node> clazz;

    NodeTypeEnum(String type, Class<? extends Node> clazz) {
        this.type = type;
        this.clazz = clazz;
    }

    public static NodeTypeEnum getEnumByType(String type) {
        for (NodeTypeEnum anEnum : values()) {
            if (anEnum.getType().equals(type)) {
                return anEnum;
            }
        }
        return null;
    }

    public static Class<? extends Node> getClazzByNode(FlowData node) {
        NodeTypeEnum typeEnum = getEnumByType(node.getType());
        if (typeEnum == null) {
            throw new InternalException("cannot find node type " + node.getType());
        }
        return typeEnum.getClazz();
    }
}
