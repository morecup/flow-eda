-- ----------------------------
-- Table structure for flow_instance
-- 流程实例表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `flow_instance` (
    `id`               VARCHAR(64) PRIMARY KEY COMMENT '实例ID',
    `flow_id`          VARCHAR(64) NOT NULL COMMENT '流程定义ID',
    `status`           VARCHAR(32) NOT NULL COMMENT '实例状态',
    `trigger_user`     VARCHAR(64) NULL COMMENT '触发用户',
    `terminate_reason` VARCHAR(255) NULL COMMENT '终止原因',
    `retry_count`      INT DEFAULT 0 COMMENT '重试次数',
    `start_time`       TIMESTAMP NULL COMMENT '开始时间',
    `end_time`         TIMESTAMP NULL COMMENT '结束时间',
    `created_at`       TIMESTAMP NOT NULL COMMENT '创建时间',
    `updated_at`       TIMESTAMP NOT NULL COMMENT '更新时间'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_flow_instance_flow_id ON flow_instance(flow_id);
CREATE INDEX idx_flow_instance_status ON flow_instance(status);
