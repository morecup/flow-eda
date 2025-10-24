-- ----------------------------
-- Table structure for flow_instance_node
-- 流程实例节点快照表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `flow_instance_node` (
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增ID',
    `instance_id`   VARCHAR(64) NOT NULL COMMENT '实例ID',
    `node_id`       VARCHAR(64) NOT NULL COMMENT '节点ID',
    `node_name`     VARCHAR(128) NULL COMMENT '节点名称',
    `node_type`     VARCHAR(64) NULL COMMENT '节点类型',
    `status`        VARCHAR(32) NOT NULL COMMENT '节点状态',
    `duration_ms`   BIGINT NULL COMMENT '执行耗时(毫秒)',
    `input_json`    TEXT NULL COMMENT '输入参数JSON',
    `output_json`   TEXT NULL COMMENT '输出参数JSON',
    `error_stack`   TEXT NULL COMMENT '错误堆栈',
    `start_time`    TIMESTAMP NULL COMMENT '开始时间',
    `end_time`      TIMESTAMP NULL COMMENT '结束时间',
    `created_at`    TIMESTAMP NOT NULL COMMENT '创建时间',
    `updated_at`    TIMESTAMP NOT NULL COMMENT '更新时间'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_flow_instance_node_instance ON flow_instance_node(instance_id);
CREATE INDEX idx_flow_instance_node_status ON flow_instance_node(status);
