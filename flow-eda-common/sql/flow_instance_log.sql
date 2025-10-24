-- ----------------------------
-- Table structure for flow_instance_log
-- 流程实例日志记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `flow_instance_log` (
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增ID',
    `instance_id`  VARCHAR(64) NOT NULL COMMENT '实例ID',
    `node_id`      VARCHAR(64) NULL COMMENT '节点ID',
    `level`        VARCHAR(16) NOT NULL COMMENT '日志级别',
    `category`     VARCHAR(64) NULL COMMENT '日志分类',
    `message`      VARCHAR(1024) NULL COMMENT '日志消息',
    `payload_json` TEXT NULL COMMENT '负载数据JSON',
    `log_time`     TIMESTAMP NOT NULL COMMENT '日志时间',
    `created_at`   TIMESTAMP NOT NULL COMMENT '创建时间'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_flow_instance_log_instance ON flow_instance_log(instance_id);
CREATE INDEX idx_flow_instance_log_level ON flow_instance_log(level);
