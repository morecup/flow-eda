-- Flow EDA Runner服务表结构
-- 流程实例执行相关表

-- 流程实例表
CREATE TABLE IF NOT EXISTS flow_instance (
    id VARCHAR(64) PRIMARY KEY COMMENT '实例ID',
    flow_id VARCHAR(64) NOT NULL COMMENT '流程ID',
    status VARCHAR(32) NOT NULL COMMENT '状态: RUNNING/FINISHED/FAILED/TERMINATED',
    trigger_user VARCHAR(64) NULL COMMENT '触发用户',
    terminate_reason VARCHAR(255) NULL COMMENT '终止原因',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    start_time TIMESTAMP NULL COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_flow_id (flow_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例表';

-- 实例节点状态表
CREATE TABLE IF NOT EXISTS flow_instance_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '节点实例ID',
    instance_id VARCHAR(64) NOT NULL COMMENT '实例ID',
    node_id VARCHAR(64) NOT NULL COMMENT '节点ID',
    node_name VARCHAR(128) NULL COMMENT '节点名称',
    node_type VARCHAR(64) NULL COMMENT '节点类型',
    `job_id`        BIGINT NULL COMMENT '外部任务ID',
    status VARCHAR(32) NOT NULL COMMENT '状态: PENDING/RUNNING/SUCCESS/FAILED/SKIPPED',
    duration_ms BIGINT NULL COMMENT '执行时长(毫秒)',
    input_json TEXT NULL COMMENT '输入数据JSON',
    output_json TEXT NULL COMMENT '输出数据JSON',
    error_stack TEXT NULL COMMENT '错误堆栈',
    start_time TIMESTAMP NULL COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_instance_id (instance_id),
    INDEX idx_status (status),
    UNIQUE INDEX uk_instance_node (instance_id, node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实例节点状态表';

-- 实例日志表
CREATE TABLE IF NOT EXISTS flow_instance_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    instance_id VARCHAR(64) NOT NULL COMMENT '实例ID',
    node_id VARCHAR(64) NULL COMMENT '节点ID',
    level VARCHAR(16) NOT NULL COMMENT '日志级别: DEBUG/INFO/WARN/ERROR',
    category VARCHAR(64) NULL COMMENT '日志分类',
    message VARCHAR(1024) NULL COMMENT '日志消息',
    payload_json TEXT NULL COMMENT '附加数据JSON',
    log_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_instance_id (instance_id),
    INDEX idx_level (level),
    INDEX idx_log_time (log_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实例日志表';
